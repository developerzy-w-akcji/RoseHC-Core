package pl.rosehc.platform.listener.player;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformConfiguration.ProxyWhitelistWrapper;
import pl.rosehc.platform.PlatformPlugin;

public final class PlayerPreLoginListener implements Listener {

  private final PlatformPlugin plugin;

  public PlayerPreLoginListener(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onLogin(final PreLoginEvent event) {
    if (!event.isCancelled()) {
      final ProxyWhitelistWrapper proxyWhitelistWrapper = this.plugin.getPlatformConfiguration().proxyWhitelistWrapper;
      if (proxyWhitelistWrapper.enabled && !proxyWhitelistWrapper.players.contains(
          event.getConnection().getName())) {
        event.setCancelled(true);
        event.setCancelReason(
            TextComponent.fromLegacyText(ChatHelper.colored(proxyWhitelistWrapper.reason)));
      }
    }
  }
}
