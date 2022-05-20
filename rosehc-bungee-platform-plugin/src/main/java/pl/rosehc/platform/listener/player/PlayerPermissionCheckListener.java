package pl.rosehc.platform.listener.player;

import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;

public final class PlayerPermissionCheckListener implements Listener {

  private final PlatformPlugin plugin;

  public PlayerPermissionCheckListener(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCheck(final PermissionCheckEvent event) {
    if (event.getSender() instanceof ProxiedPlayer) {
      final ProxiedPlayer player = (ProxiedPlayer) event.getSender();
      final Optional<PlatformUser> platformUserOptional = this.plugin.getPlatformUserFactory()
          .findUserByUniqueId(player.getUniqueId());
      if (!platformUserOptional.isPresent()) {
        event.setHasPermission(false);
        return;
      }

      final List<String> permissions = platformUserOptional.get().getRank().getCurrentRank()
          .getPermissions();
      event.setHasPermission(permissions.contains("*") || permissions.stream().anyMatch(
          permission -> !permission.startsWith("-") && permission.equalsIgnoreCase(
              event.getPermission())));
    }
  }
}
