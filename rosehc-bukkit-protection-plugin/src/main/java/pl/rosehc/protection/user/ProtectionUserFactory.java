package pl.rosehc.protection.user;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class ProtectionUserFactory {

  private final Map<UUID, ProtectionUser> protectionUserMap = new ConcurrentHashMap<>();

  public void addUser(final ProtectionUser user) {
    this.protectionUserMap.put(user.getUniqueId(), user);
  }

  public void removeUser(final ProtectionUser user) {
    this.protectionUserMap.remove(user.getUniqueId());
  }

  public Optional<ProtectionUser> findUser(final Player player) {
    return Optional.ofNullable(this.protectionUserMap.get(player.getUniqueId()));
  }
}
