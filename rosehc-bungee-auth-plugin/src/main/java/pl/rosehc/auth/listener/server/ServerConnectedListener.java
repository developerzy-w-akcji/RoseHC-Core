package pl.rosehc.auth.listener.server;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.auth.user.AuthUserQueueHelper;

public final class ServerConnectedListener implements Listener {

  private final AuthPlugin plugin;

  public ServerConnectedListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onConnected(final ServerConnectedEvent event) {
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(event.getPlayer());
    if (Objects.nonNull(user) && event.getServer().getInfo().getName()
        .equals(this.plugin.getAuthConfiguration().queueServerName) && user.isBlazingAuthenticated()
        && user.isLogged() && user.isKickedFromServer()) {
      this.plugin.getProxy().getScheduler()
          .schedule(this.plugin, () -> AuthUserQueueHelper.addToQueue(event.getPlayer()), 500L,
              TimeUnit.MILLISECONDS);
    }
  }
}
