package pl.rosehc.achievements.user;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class AchievementsUserFactory {

  private final Map<UUID, AchievementsUser> userMap = new ConcurrentHashMap<>();

  public void addUser(final AchievementsUser user) {
    this.userMap.put(user.getUniqueId(), user);
  }

  public void removeUser(final AchievementsUser user) {
    this.userMap.remove(user.getUniqueId());
  }

  public Optional<AchievementsUser> findUserByPlayer(final Player player) {
    return this.findUserByUniqueId(player.getUniqueId());
  }

  public Optional<AchievementsUser> findUserByUniqueId(final UUID uniqueId) {
    return Optional.ofNullable(this.userMap.get(uniqueId));
  }

  public Optional<AchievementsUser> findUserByNickname(final String nickname) {
    for (final AchievementsUser user : this.userMap.values()) {
      if (user.getNickname().equals(nickname)) {
        return Optional.of(user);
      }
    }

    return Optional.empty();
  }

  public Map<UUID, AchievementsUser> getUserMap() {
    return this.userMap;
  }
}
