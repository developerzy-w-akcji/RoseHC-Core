package pl.rosehc.guilds.listener.sector;

import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.guilds.GuildsConfiguration.MessagesWrapper.TitleMessageWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.sectors.sector.user.SectorUserJoinEvent;

public final class SectorUserJoinListener implements Listener {

  private final GuildsPlugin plugin;

  public SectorUserJoinListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(final SectorUserJoinEvent event) {
    final Player player = event.getPlayer();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
      final Guild guild = user.getGuild();
      if (guild != null) {
        final long validityTimeDelta = guild.getValidityTime() - System.currentTimeMillis();
        if (validityTimeDelta
            <= this.plugin.getGuildsConfiguration().pluginWrapper.parsedMinGuildValidityTime) {
          final TitleMessageWrapper guildWillSoonExpireJoinTitle = this.plugin.getGuildsConfiguration().messagesWrapper.guildWillSoonExpireJoinTitle;
          ChatHelper.sendTitle(player, guildWillSoonExpireJoinTitle.title.replace("{TIME}",
                  TimeHelper.timeToString(validityTimeDelta)),
              guildWillSoonExpireJoinTitle.subTitle.replace("{TIME}",
                  TimeHelper.timeToString(validityTimeDelta)), guildWillSoonExpireJoinTitle.fadeIn,
              guildWillSoonExpireJoinTitle.stay, guildWillSoonExpireJoinTitle.fadeOut);
        }

        if (guild.getJoinAlertMessage() != null) {
          ChatHelper.sendMessage(player, guild.getJoinAlertMessage());
        }
      }

      final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
      connection.sendPacket(new PacketPlayOutCustomPayload("BP|ShowPingOnTab",
          new PacketDataSerializer(
              Unpooled.wrappedBuffer("false".getBytes(StandardCharsets.UTF_8)))));
      connection.sendPacket(new PacketPlayOutCustomPayload("bp:heads",
          new PacketDataSerializer(Unpooled.buffer().writeByte(2))));
    });
  }
}
