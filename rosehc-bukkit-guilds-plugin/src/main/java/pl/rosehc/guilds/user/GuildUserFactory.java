package pl.rosehc.guilds.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import pl.rosehc.controller.wrapper.guild.GuildUserSerializableWrapper;
import pl.rosehc.guilds.GuildsPlugin;

public final class GuildUserFactory {

  private final Map<UUID, GuildUser> guildUserMap;

  public GuildUserFactory(final List<GuildUserSerializableWrapper> users) {
    this.guildUserMap = new ConcurrentHashMap<>();
    for (final GuildUserSerializableWrapper user : users) {
      this.guildUserMap.put(user.getUniqueId(), GuildUser.create(user));
    }

    GuildsPlugin.getInstance().getLogger()
        .log(Level.INFO, "Załadowano " + this.guildUserMap.size() + " użytkowników.");
  }

  public void addUser(final GuildUser user) {
    this.guildUserMap.put(user.getUniqueId(), user);
  }

  public Optional<GuildUser> findUserByUniqueId(final UUID uniqueId) {
    return Optional.ofNullable(this.guildUserMap.get(uniqueId));
  }

  public Optional<GuildUser> findUserByNickname(final String nickname) {
    for (final GuildUser user : this.guildUserMap.values()) {
      if (user.getNickname().equalsIgnoreCase(nickname)) {
        return Optional.of(user);
      }
    }

    return Optional.empty();
  }

  public Map<UUID, GuildUser> getGuildUserMap() {
    return this.guildUserMap;
  }
}
