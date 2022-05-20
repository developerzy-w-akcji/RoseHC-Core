package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.platform.PlatformConfiguration.CustomItemType;
import pl.rosehc.platform.user.event.PlatformUserUseCustomItemEvent;

public final class PlatformUserUseCustomItemListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlatformUserUseCustomItemListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onUse(final PlatformUserUseCustomItemEvent event) {
    if (event.getCustomItemType().equals(CustomItemType.GHEAD)) {
      this.plugin.getAchievementsUserFactory().findUserByPlayer(event.getPlayer()).ifPresent(
          user -> user.incrementAchievementStatistic(AchievementType.EATEN_GOLDEN_HEADS, 1));
    }
  }
}
