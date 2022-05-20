package pl.rosehc.guilds.guild.task;

import org.bukkit.entity.Player;
import pl.rosehc.actionbar.PrioritizedActionBarConstants;
import pl.rosehc.actionbar.PrioritizedActionBarPlugin;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildPistonScanningInformationTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildPistonScanningInformationTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 20L, 20L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId())
          .map(GuildUser::getGuild).map(Guild::getPistonBlockScanner).ifPresent(
              pistonBlockScanner -> PrioritizedActionBarPlugin.getInstance()
                  .getPrioritizedActionBarFactory().updateActionBar(player.getUniqueId(),
                      ChatHelper.colored(pistonBlockScanner.isPreparing()
                          ? this.plugin.getGuildsConfiguration().messagesWrapper.guildPistonScannerPreparingActionBarMessage
                          : this.plugin.getGuildsConfiguration().messagesWrapper.guildPistonScannerUpdatingActionBarMessage.replace(
                                  "{PROCESSED_BLOCKS}",
                                  String.valueOf(pistonBlockScanner.getProcessedBlocks()))
                              .replace("{MAX_BLOCKS}",
                                  String.valueOf(pistonBlockScanner.getMaxBlocksToScan()))
                              .replace("{PERCENTAGE}", String.format("%.2f",
                                  pistonBlockScanner.getProcessedBlocks() * 100D
                                      / pistonBlockScanner.getMaxBlocksToScan()))),
                      PrioritizedActionBarConstants.GUILD_SCANNER_ACTION_BAR_PRIORITY));
    }
  }
}
