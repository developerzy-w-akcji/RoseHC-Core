package pl.rosehc.randomtp.system.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class PlayerCommandPreprocessListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerCommandPreprocessListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCommand(final PlayerCommandPreprocessEvent event) {
    final Player player = event.getPlayer();
    if (player.isOp()) {
      return;
    }

    if (!this.plugin.getRandomTPConfiguration().allowedCommandList.contains(
        event.getMessage().split(" ")[0].toLowerCase())) {
      event.setCancelled(true);
      ChatHelper.sendMessage(player,
          this.plugin.getRandomTPConfiguration().cannotExecuteThisCommandOnThisSector);
    }
  }
}
