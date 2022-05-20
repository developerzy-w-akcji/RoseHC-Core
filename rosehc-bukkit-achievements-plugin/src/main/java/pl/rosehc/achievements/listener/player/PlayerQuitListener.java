package pl.rosehc.achievements.listener.player;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;

public final class PlayerQuitListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlayerQuitListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByPlayer(event.getPlayer()).ifPresent(user -> {
      this.plugin.getAchievementsUserFactory().removeUser(user);
      if (user.isNeedUpdate()) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
          try {
            user.incrementAchievementStatistic(AchievementType.SPEND_TIME,
                System.currentTimeMillis() - user.getLastTimeMeasurement());
            this.plugin.getAchievementsUserRepository().update(user);
          } catch (final SQLException ex) {
            this.plugin.getLogger()
                .log(Level.SEVERE, "Nie można było wykonać update'u użytkownika.", ex);
          }
        });
      }
    });
  }
}
