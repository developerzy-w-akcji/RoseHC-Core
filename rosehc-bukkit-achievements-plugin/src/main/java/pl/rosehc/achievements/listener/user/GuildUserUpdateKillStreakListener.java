package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.guilds.user.event.GuildUserUpdateKillStreakEvent;

public final class GuildUserUpdateKillStreakListener implements Listener {

  private final AchievementsPlugin plugin;

  public GuildUserUpdateKillStreakListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onKillStreakUpdate(final GuildUserUpdateKillStreakEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByUniqueId(event.getUser().getUniqueId())
        .ifPresent(user -> user.incrementAchievementStatistic(AchievementType.KILLSTREAK, 1));
  }
}
