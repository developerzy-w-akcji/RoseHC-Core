package pl.rosehc.achievements.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.adapter.helper.LocationHelper;

public final class PlayerMoveListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlayerMoveListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onMove(final PlayerMoveEvent event) {
    if (LocationHelper.isSameLocationXZ(event.getFrom(), event.getTo())) {
      return;
    }

    final Player player = event.getPlayer();
    this.plugin.getAchievementsUserFactory().findUserByPlayer(player).ifPresent(
        user -> user.incrementAchievementStatistic(AchievementType.TRAVELED_KILOMETERS, 0.1D));
  }
}
