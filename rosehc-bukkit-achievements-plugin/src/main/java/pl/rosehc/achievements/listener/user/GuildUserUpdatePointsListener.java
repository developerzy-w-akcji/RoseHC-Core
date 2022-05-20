package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.guilds.user.event.GuildUserUpdatePointsEvent;

public final class GuildUserUpdatePointsListener implements Listener {

  private final AchievementsPlugin plugin;

  public GuildUserUpdatePointsListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPointsUpdate(final GuildUserUpdatePointsEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByUniqueId(event.getUser().getUniqueId())
        .ifPresent(user -> {
          final double killsAndDeathsPointBoost = user.getAchievementBoost(
              AchievementBoostType.KILL_AND_DEATH_POINTS), killsPoints = user.getAchievementBoost(
              AchievementBoostType.KILL_POINTS);
          final int points = event.getPoints();
          if (!event.isDeath()) {
            user.incrementAchievementStatistic(AchievementType.POINTS, points);
          }

          if (killsAndDeathsPointBoost != -1D || killsPoints != -1D) {
            event.setPoints(
                !event.isDeath() || killsAndDeathsPointBoost == -1D ? (int) (points + (points * (
                    (killsAndDeathsPointBoost != -1D ? killsAndDeathsPointBoost : killsPoints)
                        / 100D))) : (int) (points - (points * (killsAndDeathsPointBoost / 100D))));
          }
        });
  }
}
