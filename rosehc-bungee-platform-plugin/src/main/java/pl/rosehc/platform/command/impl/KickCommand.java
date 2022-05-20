package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.user.PlatformUserKickPacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class KickCommand {

  private final PlatformPlugin plugin;

  public KickCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-kick")
  @Command(value = "kick", description = "Wyrzuca gracza z jego serwera proxy.")
  public void handleKick(final @Sender CommandSender sender, final @Name("player") SectorUser user,
      final @Name("reason") @Combined String reason) {
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.successfullyKicked.replace(
            "{PLAYER_NAME}", user.getNickname()).replace("{REASON}", reason));
    this.plugin.getRedisAdapter().sendPacket(new PlatformUserKickPacket(user.getUniqueId(), reason),
        "rhc_platform_" + user.getProxy().getIdentifier());
  }
}
