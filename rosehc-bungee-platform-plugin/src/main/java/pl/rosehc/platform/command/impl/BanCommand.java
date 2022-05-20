package pl.rosehc.platform.command.impl;

import java.util.Objects;
import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Flag;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Optional;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.ban.PlatformBanBroadcastPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanCreatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserKickPacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.ban.Ban;
import pl.rosehc.platform.ban.BanCreateEvent;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.sectors.SectorsPlugin;

public final class BanCommand {

  private final PlatformPlugin plugin;

  public BanCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-permban")
  @Command(value = "permban", description = "Banuje podanego gracza permanentnie.")
  public void handlePermBan(final @Sender CommandSender sender,
      final @Flag(value = 's', description = "Czy ban ma być ukryty?") boolean silent,
      final @Name("player") PlatformUser user,
      final @Optional("Brak!") @Combined @Name("reason") String reason) {
    if (this.plugin.getBanFactory().findBan(user.getNickname()).isPresent()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.banUserIsAlreadyBanned));
    }

    final Ban ban = new Ban(user.getNickname(), sender.getName(), "-", reason,
        Objects.nonNull(user.getComputerUid()) ? user.getComputerUid() : new byte[0],
        System.currentTimeMillis(), 0L);
    final BanCreateEvent event = this.plugin.getProxy().getPluginManager()
        .callEvent(new BanCreateEvent(ban));
    if (event.isCancelled()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.banCannotBeCreated));
    }

    this.plugin.getBanFactory().addBan(ban);
    this.plugin.getRedisAdapter().sendPacket(
        new PlatformBanCreatePacket(ban.getPlayerNickname(), ban.getStaffNickname(),
            ban.getReason(), ban.getIp(), ban.getComputerUid(), ban.getCreationTime(), 0L),
        "rhc_master_controller", "rhc_platform");
    this.plugin.getRedisAdapter().sendPacket(new PlatformBanBroadcastPacket(
            (silent ? this.plugin.getPlatformConfiguration().messagesWrapper.banPermBroadcastSilent
                : this.plugin.getPlatformConfiguration().messagesWrapper.banPermBroadcastGlobal).replace(
                    "{PLAYER_NAME}", ban.getPlayerNickname())
                .replace("{STAFF_NAME}", ban.getStaffNickname()).replace("{REASON}", reason), silent),
        "rhc_platform");
    SectorsPlugin.getInstance().getSectorUserFactory().findUserByUniqueId(user.getUniqueId())
        .ifPresent(sectorUser -> this.plugin.getRedisAdapter().sendPacket(
            new PlatformUserKickPacket(user.getUniqueId(),
                this.plugin.getPlatformConfiguration().messagesWrapper.banKickCommandPerm.replace(
                        "{PLAYER_NAME}", ban.getPlayerNickname())
                    .replace("{STAFF_NAME}", ban.getStaffNickname())
                    .replace("{CREATION_TIME}", TimeHelper.dateToString(ban.getCreationTime()))
                    .replace("{REASON}", reason)), "rhc_platform_" + sectorUser.getProxy()));
  }

  @Permission("platform-command-tempban")
  @Command(value = "tempban", description = "Banuje podanego gracza tymczasowo.")
  public void handleTempBan(final @Sender CommandSender sender,
      final @Flag(value = 's', description = "Czy ban ma być ukryty?") boolean silent,
      final @Name("player") PlatformUser user, final @Name("time") String time,
      final @Optional("Brak!") @Combined @Name("reason") String reason) {
    if (this.plugin.getBanFactory().findBan(user.getNickname()).isPresent()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.banUserIsAlreadyBanned));
    }

    final long parsedTime = TimeHelper.timeFromString(time);
    final Ban ban = new Ban(user.getNickname(), sender.getName(), "-", reason,
        Objects.nonNull(user.getComputerUid()) ? user.getComputerUid() : new byte[0],
        System.currentTimeMillis(), System.currentTimeMillis() + parsedTime);
    final BanCreateEvent event = this.plugin.getProxy().getPluginManager()
        .callEvent(new BanCreateEvent(ban));
    if (event.isCancelled()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.banCannotBeCreated));
    }

    this.plugin.getBanFactory().addBan(ban);
    this.plugin.getRedisAdapter().sendPacket(
        new PlatformBanCreatePacket(ban.getPlayerNickname(), ban.getStaffNickname(),
            ban.getReason(), ban.getIp(), ban.getComputerUid(), ban.getCreationTime(),
            ban.getLeftTime()), "rhc_master_controller", "rhc_platform");
    this.plugin.getRedisAdapter().sendPacket(new PlatformBanBroadcastPacket(
        (silent ? this.plugin.getPlatformConfiguration().messagesWrapper.banTempBroadcastSilent
            : this.plugin.getPlatformConfiguration().messagesWrapper.banTempBroadcastGlobal).replace(
                "{PLAYER_NAME}", ban.getPlayerNickname())
            .replace("{STAFF_NAME}", ban.getStaffNickname())
            .replace("{TIME}", TimeHelper.timeToString(parsedTime)).replace("{REASON}", reason),
        silent), "rhc_platform");
    SectorsPlugin.getInstance().getSectorUserFactory().findUserByUniqueId(user.getUniqueId())
        .ifPresent(sectorUser -> this.plugin.getRedisAdapter().sendPacket(
            new PlatformUserKickPacket(user.getUniqueId(),
                this.plugin.getPlatformConfiguration().messagesWrapper.banKickCommandTemp.replace(
                        "{PLAYER_NAME}", ban.getPlayerNickname())
                    .replace("{STAFF_NAME}", ban.getStaffNickname())
                    .replace("{CREATION_TIME}", TimeHelper.dateToString(ban.getCreationTime()))
                    .replace("{LEFT_TIME}", TimeHelper.timeToString(parsedTime))
                    .replace("{REASON}", reason)), "rhc_platform_" + sectorUser.getProxy()));
  }
}
