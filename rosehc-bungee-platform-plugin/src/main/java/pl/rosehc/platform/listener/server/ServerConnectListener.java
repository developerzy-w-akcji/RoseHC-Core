package pl.rosehc.platform.listener.server;

import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCreatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserNicknameUpdatePacket;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;

public final class ServerConnectListener implements Listener {

  private final PlatformPlugin plugin;

  public ServerConnectListener(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onConnect(final ServerConnectEvent event) {
    if (!event.getReason().equals(Reason.JOIN_PROXY)) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(event.getPlayer().getUniqueId())
          .map(PlatformUser::getComputerUid).flatMap(computerUid -> this.plugin.getBanFactory()
              .findBan(event.getPlayer().getName(),
                  event.getPlayer().getAddress().getAddress().getHostAddress(), computerUid))
          .ifPresent(ban -> {
            if (!ban.isPerm() && ban.getLeftTime() <= System.currentTimeMillis()) {
              this.plugin.getBanFactory().removeBan(ban);
              this.plugin.getRedisAdapter()
                  .sendPacket(new PlatformBanDeletePacket(ban.getPlayerNickname()),
                      "rhc_master_controller", "rhc_platform");
              return;
            }

            event.setCancelled(true);
            event.getPlayer().disconnect(TextComponent.fromLegacyText(ChatHelper.colored(
                (!ban.isPerm()
                    ? this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinTemp.replace(
                    "{LEFT_TIME}",
                    TimeHelper.timeToString(ban.getLeftTime() - System.currentTimeMillis()))
                    : this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinPerm).replace(
                        "{PLAYER_NAME}", ban.getPlayerNickname()).replace("{REASON}", ban.getReason())
                    .replace("{CREATION_TIME}", TimeHelper.dateToString(ban.getCreationTime()))
                    .replace("{STAFF_NAME}", ban.getStaffNickname()))));
          });
    }

    if (!event.getReason().equals(Reason.JOIN_PROXY) || event.isCancelled()) {
      return;
    }

    final ProxiedPlayer player = event.getPlayer();
    final PlatformUser user = this.plugin.getPlatformUserFactory()
        .findUserByUniqueId(player.getUniqueId())
        .orElseGet(() -> this.createUser(player.getUniqueId(), player.getName()));
    if (user.isNewlyCreated()) {
      user.setNewlyCreated(false);
      event.setCancelled(true);
      player.disconnect(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.accountCreated));
      return;
    }

    if (!user.getNickname().equals(player.getName())) {
      user.setNickname(player.getName());
      this.plugin.getRedisAdapter()
          .sendPacket(new PlatformUserNicknameUpdatePacket(user.getUniqueId(), user.getNickname()),
              "rhc_master_controller", "rhc_platform");
    }

    if (!player.hasPermission("platform-slots-bypass") && this.plugin.getProxy().getOnlineCount()
        >= this.plugin.getPlatformConfiguration().slotWrapper.proxySlots) {
      event.setCancelled(true);
      player.disconnect(
          ChatHelper.colored(this.plugin.getPlatformConfiguration().messagesWrapper.proxyIsFull));
      return;
    }

    if (!player.hasPermission("platform-join-cooldown-bypass") && user.getCooldownCache()
        .hasUserCooldown(PlatformUserCooldownType.PROXY_JOIN)) {
      event.setCancelled(true);
      player.disconnect(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.proxyJoinIsCooldowned.replace(
              "{TIME}", TimeHelper.timeToString(
                  user.getCooldownCache().getUserCooldown(PlatformUserCooldownType.PROXY_JOIN)))));
      return;
    }

    user.getCooldownCache().putUserCooldown(PlatformUserCooldownType.PROXY_JOIN);
  }

  private PlatformUser createUser(final UUID uniqueId, final String nickname) {
    final PlatformUser user = new PlatformUser(uniqueId, nickname);
    user.setNewlyCreated(true);
    this.plugin.getPlatformUserFactory().addUser(user);
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformUserCreatePacket(user.getUniqueId(), user.getNickname()),
            "rhc_master_controller", "rhc_platform");
    return user;
  }
}
