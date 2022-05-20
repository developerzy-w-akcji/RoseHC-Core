package pl.rosehc.auth.user;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;

public final class AuthUserCheckCachedProfileTask implements Runnable {

  private final AuthPlugin plugin;

  public AuthUserCheckCachedProfileTask(final AuthPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getProxy().getScheduler().schedule(this.plugin, this, 1L, 1L, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    for (final ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
      final AuthUser user = this.plugin.getAuthUserFactory().findUser(player);
      if (Objects.isNull(user)
          || player.getPendingConnection().isOnlineMode() != user.isPremium()) {
        player.disconnect(TextComponent.fromLegacyText(
            ChatHelper.colored(this.plugin.getAuthConfiguration().profileErrorOccurred)));
      }
    }
  }
}
