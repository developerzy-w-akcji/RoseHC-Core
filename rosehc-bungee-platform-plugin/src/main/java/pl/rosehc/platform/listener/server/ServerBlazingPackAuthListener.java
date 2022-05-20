package pl.rosehc.platform.listener.server;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.blazingpack.bpauth.BlazingPackAuthEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserComputerUidUpdatePacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUserBlazingAuthCancellable;

public final class ServerBlazingPackAuthListener implements Listener {

  private final PlatformPlugin plugin;

  public ServerBlazingPackAuthListener(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void handleAuth(final BlazingPackAuthEvent event) {
    final UserConnection userConnection = event.getUserConnection();
    if (!event.isUpToDate()) {
      PlatformUserBlazingAuthCancellable.cancel(event);
      userConnection.disconnect(TextComponent.fromLegacyText(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.blazingPackIsOutdated)));
      return;
    }

    this.plugin.getBanFactory().findBan(userConnection.getName(),
            userConnection.getAddress().getAddress().getHostAddress(), event.getComputerUUID())
        .ifPresent(ban -> {
          if (!ban.isPerm() && ban.getLeftTime() <= System.currentTimeMillis()) {
            this.plugin.getBanFactory().removeBan(ban);
            this.plugin.getRedisAdapter()
                .sendPacket(new PlatformBanDeletePacket(ban.getPlayerNickname()),
                    "rhc_master_controller", "rhc_platform");
            return;
          }

          PlatformUserBlazingAuthCancellable.cancel(event);
          userConnection.disconnect(TextComponent.fromLegacyText(ChatHelper.colored((!ban.isPerm()
              ? this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinTemp.replace(
              "{LEFT_TIME}",
              TimeHelper.timeToString(ban.getLeftTime() - System.currentTimeMillis()))
              : this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinPerm).replace(
                  "{PLAYER_NAME}", ban.getPlayerNickname()).replace("{REASON}", ban.getReason())
              .replace("{CREATION_TIME}", TimeHelper.dateToString(ban.getCreationTime()))
              .replace("{STAFF_NAME}", ban.getStaffNickname()))));
        });
    this.plugin.getPlatformUserFactory().findUserByUniqueId(userConnection.getUniqueId())
        .ifPresent(user -> {
          user.setComputerUid(event.getComputerUUID());
          this.plugin.getRedisAdapter().sendPacket(
              new PlatformUserComputerUidUpdatePacket(user.getUniqueId(), user.getComputerUid()),
              "rhc_master_controller", "rhc_platform");
        });
  }
}
