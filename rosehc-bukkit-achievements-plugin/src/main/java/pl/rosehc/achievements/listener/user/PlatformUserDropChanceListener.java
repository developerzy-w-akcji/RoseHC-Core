package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.platform.user.event.PlatformUserDropChanceEvent;

public final class PlatformUserDropChanceListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlatformUserDropChanceListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onChance(final PlatformUserDropChanceEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByUniqueId(event.getUser().getUniqueId())
        .ifPresent(user -> {
          final double chanceBoost = user.getAchievementBoost(AchievementBoostType.DROP_CHANCE);
          if (chanceBoost != -1D) {
            event.setChance(event.getChance() + (event.getChance() * (chanceBoost / 100D)));
          }
        });
  }
}
