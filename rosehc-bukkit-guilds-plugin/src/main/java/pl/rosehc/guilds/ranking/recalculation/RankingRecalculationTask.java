package pl.rosehc.guilds.ranking.recalculation;

import pl.rosehc.guilds.GuildsPlugin;

public final class RankingRecalculationTask implements Runnable {

  private final GuildsPlugin plugin;

  public RankingRecalculationTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0L, 1200L);
  }

  @Override
  public void run() {
    this.plugin.getRankingFactory().updateAll();
  }
}
