package pl.rosehc.auth.user;

import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.auth.AuthPlugin;

public final class AuthUserTimeoutTask implements Runnable {

  private final AuthPlugin plugin;

  public AuthUserTimeoutTask(final AuthPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getProxy().getScheduler().schedule(this.plugin, this, 1L, 1L, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    for (final ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
      if (player.getPendingConnection().isOnlineMode()) {
        continue;
      }

      final AuthUser user = this.plugin.getAuthUserFactory().findUser(player);
      if (user.isLogged()) {
        continue;
      }

      if (user.hasTimeout()) {
        player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
            ChatHelper.colored(
                (!user.isRegistered() ? this.plugin.getAuthConfiguration().registerTimeoutInfo
                    : this.plugin.getAuthConfiguration().loginTimeoutInfo).replace("{TIME}",
                    TimeHelper.timeToString(user.getTimeout() - System.currentTimeMillis())))));
      } else {
        player.disconnect(ChatHelper.colored(
            !user.isRegistered() ? this.plugin.getAuthConfiguration().registerTimeoutReached
                : this.plugin.getAuthConfiguration().loginTimeoutReached));
      }
    }
  }
}
