package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.platform.user.event.PlatformUserDropLevelUpEvent;

public final class PlatformUserDropLevelUpListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlatformUserDropLevelUpListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLevelUp(final PlatformUserDropLevelUpEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByUniqueId(event.getUser().getUniqueId())
        .ifPresent(user -> user.incrementAchievementStatistic(AchievementType.MINING_LEVEL, 1));
  }
}
