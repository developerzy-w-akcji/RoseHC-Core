package pl.rosehc.auth.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.controller.wrapper.auth.AuthUserSerializableWrapper;

public final class AuthUserFactory {

  private final Map<String, AuthUser> userMap;

  public AuthUserFactory(final List<AuthUserSerializableWrapper> users) {
    this.userMap = new ConcurrentHashMap<>();
    for (final AuthUserSerializableWrapper wrapper : users) {
      this.userMap.put(wrapper.getNickname().toLowerCase(), AuthUser.create(wrapper));
    }

    AuthPlugin.getInstance().getLogger()
        .log(Level.INFO, "Załadowano " + this.userMap.size() + " użytkowników.");
  }

  public void addUser(final AuthUser user) {
    this.userMap.put(user.getNickname().toLowerCase(), user);
  }

  public void removeUser(final AuthUser user) {
    this.userMap.remove(user.getNickname().toLowerCase());
  }

  public boolean hasMaxAccounts(final String lastIP) {
    int accounts = 0;
    for (final AuthUser user : this.userMap.values()) {
      if (user.getLastIP().equals(lastIP)) {
        accounts++;
      }
    }

    return accounts > 3;
  }

  public AuthUser findUser(final ProxiedPlayer player) {
    return this.userMap.get(player.getName().toLowerCase());
  }

  public Optional<AuthUser> findUser(final String nickname) {
    return Optional.ofNullable(this.userMap.get(nickname.toLowerCase()));
  }
}
