package pl.rosehc.randomtp.system.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class Arena {

  private final UUID senderPlayerUniqueId, nearestPlayerUniqueId;
  private final ArenaRegion arenaRegion;
  private final Map<String, Location> placedBlockLocationMap = new HashMap<>();
  private final long creationTime;
  private long deletionTime;

  public Arena(final UUID senderPlayerUniqueId, final UUID nearestPlayerUniqueId,
      final Location centerLocation) {
    this.senderPlayerUniqueId = senderPlayerUniqueId;
    this.nearestPlayerUniqueId = nearestPlayerUniqueId;
    this.arenaRegion = new ArenaRegion(centerLocation);
    this.creationTime = System.currentTimeMillis();
  }

  public UUID getSenderPlayerUniqueId() {
    return this.senderPlayerUniqueId;
  }

  public UUID getNearestPlayerUniqueId() {
    return this.nearestPlayerUniqueId;
  }

  public ArenaRegion getArenaRegion() {
    return this.arenaRegion;
  }

  public Map<String, Location> getPlacedBlockLocationMap() {
    return this.placedBlockLocationMap;
  }

  public long getDeletionTime() {
    return this.deletionTime;
  }

  public boolean wasJustCreated() {
    return this.creationTime + SystemRandomTPPlugin.getInstance()
        .getRandomTPConfiguration().parsedCreationIdleTime > System.currentTimeMillis();
  }

  public void prepareForDeletion() {
    this.deletionTime = System.currentTimeMillis() + SystemRandomTPPlugin.getInstance()
        .getRandomTPConfiguration().parsedDeletionTime;
  }
}
