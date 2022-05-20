package pl.rosehc.guilds.guild.task;

import java.util.List;
import java.util.stream.Collectors;
import pl.rosehc.controller.packet.guild.guild.GuildAddRegenerationBlocksPacket;
import pl.rosehc.controller.wrapper.guild.GuildRegenerationBlockStateSerializationWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildRegenerationBlockState;
import pl.rosehc.sectors.SectorsPlugin;

public final class GuildUpdateRegenerationBlocksTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildUpdateRegenerationBlocksTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 20L, 20L);
  }

  @Override
  public void run() {
    final List<Guild> guildBySectorList = this.plugin.getGuildFactory()
        .getGuildsBySector(SectorsPlugin.getInstance().getSectorFactory().getCurrentSector());
    for (final Guild guild : guildBySectorList) {
      final List<GuildRegenerationBlockStateSerializationWrapper> blockStateList = guild.getNewlyAddedRegenerationBlocks()
          .stream().map(GuildRegenerationBlockState::wrap).collect(Collectors.toList());
      if (!blockStateList.isEmpty()) {
        this.plugin.getRedisAdapter()
            .sendPacket(new GuildAddRegenerationBlocksPacket(guild.getTag(), blockStateList),
                "rhc_master_controller");
      }
    }
  }
}
