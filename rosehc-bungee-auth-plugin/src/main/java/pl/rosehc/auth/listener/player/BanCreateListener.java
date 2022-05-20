package pl.rosehc.auth.listener.player;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.platform.ban.BanCreateEvent;

public final class BanCreateListener implements Listener {

  private final AuthPlugin plugin;

  public BanCreateListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onBanCreate(final BanCreateEvent event) {
    this.plugin.getAuthUserFactory().findUser(event.getBan().getPlayerNickname())
        .ifPresent(user -> event.getBan().setIp(user.getLastIP()));
  }
}
