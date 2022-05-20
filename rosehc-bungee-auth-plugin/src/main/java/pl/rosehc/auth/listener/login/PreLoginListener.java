package pl.rosehc.auth.listener.login;

import java.util.Optional;
import java.util.regex.Pattern;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.mojang.MojangRequestHelper;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.controller.packet.auth.user.AuthUserCreatePacket;

public final class PreLoginListener implements Listener {

  private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
  private final AuthPlugin plugin;
  private long lastConnect;

  public PreLoginListener(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onLoginLowest(PreLoginEvent event) {
    final PendingConnection connection = event.getConnection();
    if (!NICKNAME_PATTERN.matcher(connection.getName()).matches()) {
      event.setCancelled(true);
      event.setCancelReason(ChatHelper.colored(this.plugin.getAuthConfiguration().invalidNickname));
      return;
    }

    final Optional<AuthUser> userOptional = this.plugin.getAuthUserFactory()
        .findUser(connection.getName());
    if (!userOptional.isPresent()) {
      if (this.plugin.getAuthUserFactory()
          .hasMaxAccounts(connection.getAddress().getAddress().getHostAddress())) {
        event.setCancelled(true);
        event.setCancelReason(
            ChatHelper.colored(this.plugin.getAuthConfiguration().maxAccountsPerIp));
        return;
      }

      if (MojangRequestHelper.isLimited() || this.lastConnect + 350L > System.currentTimeMillis()) {
        event.setCancelled(true);
        event.setCancelReason(
            ChatHelper.colored(this.plugin.getAuthConfiguration().tooManyRequests));
        return;
      }

      event.registerIntent(this.plugin);
      this.lastConnect = System.currentTimeMillis();
      this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
        try {
          final AuthUser user = new AuthUser(connection.getName(),
              connection.getAddress().getAddress().getHostAddress(), System.currentTimeMillis(),
              System.currentTimeMillis(), MojangRequestHelper.fetchStatus(connection.getName()));
          this.plugin.getAuthUserFactory().addUser(user);
          this.plugin.getRedisAdapter().sendPacket(
              new AuthUserCreatePacket(user.getNickname(), user.getPassword(), user.getLastIP(),
                  user.getFirstJoinTime(), user.getLastOnlineTime(), user.isPremium(),
                  user.isRegistered()), "rhc_master_controller", "rhc_auth");
        } catch (final Exception ex) {
          event.setCancelled(true);
          event.setCancelReason(
              ChatHelper.colored(this.plugin.getAuthConfiguration().cantVerifyYourAccount));
        } finally {
          event.completeIntent(this.plugin);
        }
      });
      return;
    }

    final AuthUser user = userOptional.get();
    if (!user.isPremium() && !user.getNickname().equals(connection.getName())) {
      event.setCancelled(true);
      event.setCancelReason(ChatHelper.colored(
          this.plugin.getAuthConfiguration().usernameDidntMatch.replace("{REAL_NICKNAME}",
              user.getNickname())));
      return;
    }

    connection.setOnlineMode(user.isPremium());
  }
}
