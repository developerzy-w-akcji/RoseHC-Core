package pl.rosehc.guilds.guild.task;

import java.util.List;
import java.util.stream.Collectors;
import pl.rosehc.controller.packet.guild.guild.GuildDeletePacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.sectors.SectorsPlugin;

public final class GuildExpiryCheckTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildExpiryCheckTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0L, 100L);
  }

  @Override
  public void run() {
    final List<Guild> expiredGuildList = this.plugin.getGuildFactory()
        .getGuildsBySector(SectorsPlugin.getInstance().getSectorFactory().getCurrentSector())
        .stream().filter(guild -> guild.getValidityTime() < System.currentTimeMillis())
        .collect(Collectors.toList());
    if (expiredGuildList.size() != 0) {
      for (final Guild guild : expiredGuildList) {
        this.plugin.getGuildFactory().unregisterGuild(guild);
        this.plugin.getRedisAdapter()
            .sendPacket(new GuildDeletePacket(guild.getTag()), "rhc_master_controller",
                "rhc_guilds");
        this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
                this.plugin.getGuildsConfiguration().messagesWrapper.guildExpiryBroadcast.replace(
                        "{TAG}", guild.getTag()).replace("{NAME}", guild.getName()).replace("{X}",
                        String.valueOf(guild.getGuildRegion().getCenterLocation().getBlockX()))
                    .replace("{Y}",
                        String.valueOf(guild.getGuildRegion().getCenterLocation().getBlockY()))
                    .replace("{Z}",
                        String.valueOf(guild.getGuildRegion().getCenterLocation().getBlockZ())), false),
            "rhc_platform");
      }
    }
  }
}
