package pl.rosehc.auth.listener.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.rosehc.auth.AuthPlugin;

public final class ServerKickListener implements Listener {

  private final AuthPlugin plugin;

  public ServerKickListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onServerKick(final ServerKickEvent event) {
    final ProxiedPlayer player = event.getPlayer();
    final ServerInfo kickedFrom = event.getKickedFrom();
    if (kickedFrom.getName().equals(this.plugin.getAuthConfiguration().queueServerName)) {
      return;
    }

    this.plugin.getAuthUserFactory().findUser(event.getPlayer().getName())
        .ifPresent(user -> user.setKickedFromServer(true));
    event.setCancelled(true);
    event.setCancelServer(
        this.plugin.getProxy().getServerInfo(this.plugin.getAuthConfiguration().queueServerName));
    player.sendMessage(event.getKickReasonComponent());
  }
}
