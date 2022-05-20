package pl.rosehc.auth.listener.player;

import java.util.Objects;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;

public final class ChatListener implements Listener {

  private final AuthPlugin plugin;

  public ChatListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onChat(final ChatEvent event) {
    if (!(event.getSender() instanceof ProxiedPlayer) || !event.isCommand()) {
      return;
    }

    final ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    final AuthUser user = plugin.getAuthUserFactory().findUser(player);
    if (Objects.isNull(user)) {
      return;
    }

    final String message = event.getMessage().split(" ")[0];
    if (user.isLogged() || message.equalsIgnoreCase("/login") || message.equalsIgnoreCase("/l")
        || message.equalsIgnoreCase("/register") || message.equalsIgnoreCase("/reg")) {
      return;
    }

    event.setCancelled(true);
    ChatHelper.sendMessage(player, this.plugin.getAuthConfiguration().notLogged);
  }
}
