package pl.rosehc.achievements.listener.user;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.guilds.user.event.GuildUserUpdateKillsEvent;

public final class GuildUserUpdateKillsListener implements Listener {

  private final AchievementsPlugin plugin;

  public GuildUserUpdateKillsListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onKillsUpdate(final GuildUserUpdateKillsEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByUniqueId(event.getUser().getUniqueId())
        .ifPresent(user -> user.incrementAchievementStatistic(AchievementType.KILLS, 1));
  }
}
