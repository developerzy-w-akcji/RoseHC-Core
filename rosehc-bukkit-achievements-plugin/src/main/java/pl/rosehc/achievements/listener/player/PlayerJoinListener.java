package pl.rosehc.achievements.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.rosehc.achievements.AchievementsPlugin;

public final class PlayerJoinListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlayerJoinListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    this.plugin.getAchievementsUserFactory().findUserByPlayer(event.getPlayer())
        .ifPresent(user -> user.setLastTimeMeasurement(System.currentTimeMillis()));
  }
}
