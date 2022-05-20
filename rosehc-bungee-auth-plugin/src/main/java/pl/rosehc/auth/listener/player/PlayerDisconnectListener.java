package pl.rosehc.auth.listener.player;

import java.util.Objects;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.controller.packet.auth.user.AuthUserLastOnlineUpdatePacket;

public final class PlayerDisconnectListener implements Listener {

  private final AuthPlugin plugin;

  public PlayerDisconnectListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onDisconnect(final PlayerDisconnectEvent event) {
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(event.getPlayer());
    if (Objects.nonNull(user)) {
      user.setLogged(false);
      user.setBlazingAuthenticated(false);
      user.setKickedFromServer(false);
      user.setLastOnlineTime(System.currentTimeMillis());
      this.plugin.getRedisAdapter().sendPacket(
          new AuthUserLastOnlineUpdatePacket(user.getNickname(), user.getLastOnlineTime()),
          "rhc_master_controller", "rhc_auth");
    }
  }
}
