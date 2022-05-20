package pl.rosehc.auth.listener.server;

import java.util.concurrent.TimeUnit;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.blazingpack.bpauth.BlazingPackAuthEvent;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUserQueueHelper;
import pl.rosehc.platform.user.PlatformUserBlazingAuthCancellable;

public final class ServerBlazingPackAuthListener implements Listener {

  private final AuthPlugin plugin;

  public ServerBlazingPackAuthListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onAuth(final BlazingPackAuthEvent event) {
    final UserConnection userConnection = event.getUserConnection();
    if (!PlatformUserBlazingAuthCancellable.isCancelled(event) && event.isUpToDate()) {
      this.plugin.getAuthUserFactory().findUser(userConnection.getName()).ifPresent(user -> {
        final boolean wasAuthenticated = user.isBlazingAuthenticated();
        user.setBlazingAuthenticated(true);
        if (user.isPremium() && !wasAuthenticated) {
          this.plugin.getProxy().getScheduler()
              .schedule(this.plugin, () -> AuthUserQueueHelper.addToQueue(userConnection), 500L,
                  TimeUnit.MILLISECONDS);
        }
      });
    }
  }
}
