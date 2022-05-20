package pl.rosehc.auth.listener.server;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;

public final class ServerConnectListener implements Listener {

  private final AuthPlugin plugin;

  public ServerConnectListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onConnect(final ServerConnectEvent event) {
    final ProxiedPlayer player = event.getPlayer();
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(player);
    if (user == null) {
      if (event.getReason().equals(Reason.JOIN_PROXY)) {
        event.setCancelled(true);
        player.disconnect(ChatHelper.colored(this.plugin.getAuthConfiguration().userDataNotFound));
      }
      return;
    }

    if (event.getReason() == Reason.JOIN_PROXY && user.isPremium()) {
      event.setTarget(
          this.plugin.getProxy().getServerInfo(this.plugin.getAuthConfiguration().queueServerName));
    } else if (!user.isLogged() && !event.getTarget().getName()
        .equals(this.plugin.getAuthConfiguration().queueServerName)) {
      event.setCancelled(true);
      ChatHelper.sendMessage(player, this.plugin.getAuthConfiguration().notLogged);
    }
  }
}
