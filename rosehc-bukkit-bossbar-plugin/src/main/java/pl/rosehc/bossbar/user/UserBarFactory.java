package pl.rosehc.bossbar.user;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class UserBarFactory {

  private final Map<UUID, UserBar> userBarMap = new ConcurrentHashMap<>();

  public void removeUserBar(final Player player) {
    this.userBarMap.remove(player.getUniqueId());
  }

  public UserBar getUserBar(final Player player) {
    return this.userBarMap.computeIfAbsent(player.getUniqueId(), ignored -> new UserBar(player));
  }

  public Optional<UserBar> findUserBar(final Player player) {
    return Optional.ofNullable(this.userBarMap.get(player.getUniqueId()));
  }
}
