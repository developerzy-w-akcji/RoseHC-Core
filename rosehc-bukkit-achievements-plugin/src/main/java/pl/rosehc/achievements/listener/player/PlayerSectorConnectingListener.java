package pl.rosehc.achievements.listener.player;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.achievements.user.AchievementsUser;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.sectors.sector.SectorConnectingEvent;

public final class PlayerSectorConnectingListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlayerSectorConnectingListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onConnect(final SectorConnectingEvent event) {
    final EventCompletionStage completionStage = event.getCompletionStage();
    final Object waiter = new Object();
    completionStage.addWaiter(waiter);
    this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
      try {
        this.plugin.getAchievementsUserFactory().findUserByPlayer(event.getPlayer())
            .filter(AchievementsUser::isNeedUpdate).ifPresent(user -> {
              try {
                user.incrementAchievementStatistic(AchievementType.SPEND_TIME,
                    System.currentTimeMillis() - user.getLastTimeMeasurement());
                user.setLastTimeMeasurement(System.currentTimeMillis());
                this.plugin.getAchievementsUserRepository().update(user);
              } catch (final SQLException ex) {
                this.plugin.getLogger()
                    .log(Level.SEVERE, "Nie można było wykonać update'u użytkownika.", ex);
              }
            });
      } finally {
        completionStage.removeWaiter(waiter);
      }
    });
  }
}
