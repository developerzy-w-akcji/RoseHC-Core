package pl.rosehc.guilds.guild.task;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.v1_8_R3.EntityGolem;
import net.minecraft.server.v1_8_R3.EntityIronGolem;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class GuildGolemUpdateTask implements Runnable {

  private final GuildsPlugin plugin;
  private final AtomicReference<Double> armorStandHeight = new AtomicReference<>(-0.25D);
  private final AtomicReference<Float> armorStandYaw = new AtomicReference<>(0F);
  private final AtomicBoolean armorStandHeightAdding = new AtomicBoolean(true);

  public GuildGolemUpdateTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 1L, 1L);
  }

  @Override
  public synchronized void run() {
    this.armorStandYaw.set((float) (this.armorStandYaw.get() + 3.16D) % 360F);
    this.armorStandHeight.set(
        this.armorStandHeight.get() + (this.armorStandHeightAdding.get() ? 0.025D : -0.025D));
    if (this.armorStandHeight.get() >= 0.25D || this.armorStandHeight.get() <= -0.25D) {
      this.armorStandHeightAdding.set(!this.armorStandHeightAdding.get());
    }

    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
        final Guild enteredGuild = user.getEnteredGuild();
        final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        final Location playerLocation = player.getLocation();
        if (enteredGuild == null || !enteredGuild.getGuildRegion().isInsideCenter(playerLocation)) {
          final EntityGolem guildGolem = user.getGuildGolem();
          if (guildGolem != null) {
            connection.sendPacket(new PacketPlayOutEntityDestroy(guildGolem.getId()));
            user.setGuildGolem(null);
          }
          return;
        }

        final Location heartLocation = enteredGuild.getGuildRegion().getCenterLocation();
        EntityGolem guildGolem = user.getGuildGolem();
        if (guildGolem == null) {
          guildGolem = new EntityIronGolem(((CraftWorld) player.getWorld()).getHandle());
          guildGolem.setCustomNameVisible(true);
          user.setGuildGolem(guildGolem);
          connection.sendPacket(new PacketPlayOutSpawnEntityLiving(guildGolem));
        }

        final Guild userGuild = user.getGuild();
        final float yaw = this.armorStandYaw.get();
        guildGolem.locX = heartLocation.getX();
        guildGolem.locY = heartLocation.getY() - 2D;
        guildGolem.locZ = heartLocation.getZ();
        guildGolem.setCustomName(ChatHelper.colored(
            this.getCustomNameRelation(userGuild, enteredGuild)
                .replace("{TAG}", enteredGuild.getTag())
                .replace("{HEALTH}", String.valueOf(enteredGuild.getHealth()))));
        connection.sendPacket(new PacketPlayOutEntityTeleport(guildGolem));
        connection.sendPacket(
            new PacketPlayOutEntityMetadata(guildGolem.getId(), guildGolem.getDataWatcher(), true));
      });
    }
  }

  private String getCustomNameRelation(final Guild playerGuild, final Guild enteredGuild) {
    if (playerGuild != null) {
      return playerGuild.equals(enteredGuild)
          ? this.plugin.getGuildsConfiguration().messagesWrapper.guildGolemTitleYour
          : playerGuild.getAlliedGuild() != null && playerGuild.getAlliedGuild()
              .equals(enteredGuild)
              ? this.plugin.getGuildsConfiguration().messagesWrapper.guildGolemTitleAlly
              : this.plugin.getGuildsConfiguration().messagesWrapper.guildGolemTitleEnemy;
    }

    return this.plugin.getGuildsConfiguration().messagesWrapper.guildGolemTitleEnemy;
  }
}
