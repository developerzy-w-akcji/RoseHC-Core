package pl.rosehc.randomtp.system.listeners;

import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.randomtp.system.arena.Arena;

public final class PlayerBlockPlaceListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerBlockPlaceListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlace(final BlockPlaceEvent event) {
    final Player player = event.getPlayer();
    final Optional<Arena> arenaOptional = this.plugin.getArenaFactory().findArenaByPlayer(player);
    if (!arenaOptional.isPresent() && !player.isOp()) {
      event.setCancelled(true);
      return;
    }

    arenaOptional.ifPresent(arena -> {
      final Location location = event.getBlockPlaced().getLocation();
      if (!arena.getArenaRegion().isInside(location)
          || arena.getDeletionTime() > System.currentTimeMillis()) {
        event.setCancelled(true);
        return;
      }

      arena.getPlacedBlockLocationMap()
          .put(location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ(),
              location);
    });
  }
}
