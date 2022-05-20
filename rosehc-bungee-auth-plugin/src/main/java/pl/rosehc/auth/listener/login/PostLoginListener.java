package pl.rosehc.auth.listener.login;

import java.util.Objects;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;

public final class PostLoginListener implements Listener {

  private final AuthPlugin plugin;

  public PostLoginListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLogin(final PostLoginEvent event) {
    final ProxiedPlayer player = event.getPlayer();
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(player);
    if (Objects.isNull(user)) {
      return;
    }

    if (Objects.isNull(user.getLastIP()) || !user.getLastIP()
        .equals(player.getAddress().getAddress().getHostAddress())) {
      user.setLastIP(player.getAddress().getAddress().getHostAddress());
    }

    if (!user.isPremium()) {
      user.resetTimeout();
      player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
          ChatHelper.colored(
              (!user.isRegistered() ? this.plugin.getAuthConfiguration().registerTimeoutInfo
                  : this.plugin.getAuthConfiguration().loginTimeoutInfo).replace("{TIME}",
                  TimeHelper.timeToString(user.getTimeout() - System.currentTimeMillis())))));
      ChatHelper.sendMessage(player,
          !user.isRegistered() ? this.plugin.getAuthConfiguration().youNeedToRegisterInfo
              : this.plugin.getAuthConfiguration().youNeedToLoginInfo);
    } else {
      user.setLogged(true);
      ChatHelper.sendMessage(player, this.plugin.getAuthConfiguration().loggedInAsPremium);
    }
  }
}
