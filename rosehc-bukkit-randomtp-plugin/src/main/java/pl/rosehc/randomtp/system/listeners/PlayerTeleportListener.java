package pl.rosehc.randomtp.system.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class PlayerTeleportListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerTeleportListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTeleport(final PlayerTeleportEvent event) {
    final Location to = event.getTo();
    if (event.getCause().equals(TeleportCause.ENDER_PEARL) && !this.plugin.getArenaFactory()
        .findArenaByPlayer(event.getPlayer()).filter(arena -> arena.getArenaRegion().isInside(to))
        .isPresent()) {
      event.setCancelled(true);
    }
  }
}
