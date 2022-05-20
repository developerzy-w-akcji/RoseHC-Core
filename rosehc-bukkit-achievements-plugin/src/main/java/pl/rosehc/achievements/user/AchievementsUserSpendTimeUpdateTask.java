package pl.rosehc.achievements.user;

import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;

public final class AchievementsUserSpendTimeUpdateTask implements Runnable {

  private final AchievementsPlugin plugin;

  public AchievementsUserSpendTimeUpdateTask(final AchievementsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler()
        .scheduleSyncRepeatingTask(this.plugin, this, 1200L, 1200L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getAchievementsUserFactory().findUserByPlayer(player).ifPresent(user -> {
        user.incrementAchievementStatistic(AchievementType.SPEND_TIME,
            System.currentTimeMillis() - user.getLastTimeMeasurement());
        user.setLastTimeMeasurement(System.currentTimeMillis());
      });
    }
  }
}
