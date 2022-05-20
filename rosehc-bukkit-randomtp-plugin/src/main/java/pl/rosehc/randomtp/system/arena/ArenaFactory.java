package pl.rosehc.randomtp.system.arena;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class ArenaFactory {

  private final Map<UUID, Arena> arenaMap = new ConcurrentHashMap<>();

  public void addArena(final Arena arena) {
    this.arenaMap.put(arena.getSenderPlayerUniqueId(), arena);
    this.arenaMap.put(arena.getNearestPlayerUniqueId(), arena);
  }

  public void removeArena(final UUID uuid) {
    this.arenaMap.remove(uuid);
  }

  public boolean canCreateArena(final Location location) {
    final int minDistance = SystemRandomTPPlugin.getInstance().getRandomTPConfiguration().cuboidSize
        + SystemRandomTPPlugin.getInstance().getRandomTPConfiguration().cuboidMinDistance;
    for (final Arena arena : this.arenaMap.values()) {
      final ArenaRegion arenaRegion = arena.getArenaRegion();
      if (arenaRegion.isInside(location)
          || arenaRegion.getCenterLocation().distanceSquared(location) <= minDistance) {
        return false;
      }
    }

    return true;
  }

  public Optional<Arena> findArenaByPlayer(final Player player) {
    return Optional.ofNullable(this.arenaMap.get(player.getUniqueId()));
  }

  public Map<UUID, Arena> getArenaMap() {
    return this.arenaMap;
  }
}
