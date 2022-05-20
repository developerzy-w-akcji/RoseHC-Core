package pl.rosehc.platform.ban;

import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;

public final class BanCheckConnectedUsersTask implements Runnable {

  private final PlatformPlugin plugin;

  public BanCheckConnectedUsersTask(final PlatformPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getProxy().getScheduler().schedule(this.plugin, this, 5L, 5L, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    for (final ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(player.getUniqueId())
          .map(PlatformUser::getComputerUid).flatMap(computerUid -> this.plugin.getBanFactory()
              .findBan(player.getName(), player.getAddress().getAddress().getHostAddress(),
                  computerUid)).ifPresent(ban -> {
            if (!ban.isPerm() && ban.getLeftTime() <= System.currentTimeMillis()) {
              this.plugin.getBanFactory().removeBan(ban);
              this.plugin.getRedisAdapter()
                  .sendPacket(new PlatformBanDeletePacket(ban.getPlayerNickname()),
                      "rhc_master_controller", "rhc_platform");
              return;
            }

            player.disconnect(TextComponent.fromLegacyText(ChatHelper.colored((!ban.isPerm()
                ? this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinTemp.replace(
                "{LEFT_TIME}", TimeHelper.timeToString(ban.getLeftTime() - System.currentTimeMillis()))
                : this.plugin.getPlatformConfiguration().messagesWrapper.banKickJoinPerm).replace(
                    "{PLAYER_NAME}", ban.getPlayerNickname()).replace("{REASON}", ban.getReason())
                .replace("{CREATION_TIME}", TimeHelper.dateToString(ban.getCreationTime()))
                .replace("{STAFF_NAME}", ban.getStaffNickname()))));
          });
    }
  }
}
