package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Flag;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.ban.PlatformBanBroadcastPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.ban.Ban;
import pl.rosehc.platform.ban.BanDeleteEvent;

public final class UnBanCommand {

  private final PlatformPlugin plugin;

  public UnBanCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = "unban", description = "Odbanowuje podanego gracza.")
  public void handleUnBan(final @Sender CommandSender sender,
      final @Flag(value = 's', description = "Czy unban ma być ukryty?") boolean silent,
      final @Name("nickname") String playerNickname) {
    final Ban ban = this.plugin.getBanFactory().findBan(playerNickname).orElseThrow(
        () -> new BladeExitMessage(ChatHelper.colored(
            this.plugin.getPlatformConfiguration().messagesWrapper.banUserIsNotBanned)));
    final BanDeleteEvent event = this.plugin.getProxy().getPluginManager()
        .callEvent(new BanDeleteEvent(ban));
    if (event.isCancelled()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.banCannotBeDeleted));
    }

    this.plugin.getBanFactory().removeBan(ban);
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformBanDeletePacket(ban.getPlayerNickname()), "rhc_master_controller",
            "rhc_platform");
    this.plugin.getRedisAdapter().sendPacket(new PlatformBanBroadcastPacket(
        (silent ? this.plugin.getPlatformConfiguration().messagesWrapper.banUnBanBroadcastSilent
            : this.plugin.getPlatformConfiguration().messagesWrapper.banUnBanBroadcastGlobal).replace(
            "{PLAYER_NAME}", ban.getPlayerNickname()).replace("{STAFF_NAME}", sender.getName()),
        silent), "rhc_platform");
  }
}
