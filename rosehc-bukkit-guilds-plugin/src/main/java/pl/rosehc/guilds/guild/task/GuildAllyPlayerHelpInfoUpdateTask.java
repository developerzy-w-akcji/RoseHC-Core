package pl.rosehc.guilds.guild.task;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoRemovePacket;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildAllyPlayerHelpInfoUpdateTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildAllyPlayerHelpInfoUpdateTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 40L, 40L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId())
          .map(GuildUser::getGuild).ifPresent(
              guild -> guild.findGuildAllyPlayerHelpInfo(player.getUniqueId()).ifPresent(helpInfo -> {
                if (helpInfo.isNotActive()) {
                  this.plugin.getRedisAdapter().sendPacket(
                      new GuildHelpInfoRemovePacket(guild.getTag(), player.getUniqueId(), true),
                      "rhc_master_controller", "rhc_guilds");
                  return;
                }

                final Location location = player.getLocation();
                helpInfo.setX(location.getBlockX());
                helpInfo.setY(location.getBlockY());
                helpInfo.setZ(location.getBlockZ());
                this.plugin.getRedisAdapter().sendPacket(
                    new GuildHelpInfoUpdatePacket(guild.getTag(), player.getUniqueId(), true,
                        helpInfo.getX(), helpInfo.getY(), helpInfo.getZ()), "rhc_master_controller",
                    "rhc_guilds");
              }));
    }
  }
}
