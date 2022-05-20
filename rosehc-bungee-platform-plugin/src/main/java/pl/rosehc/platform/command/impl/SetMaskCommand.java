package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.PlatformSetMotdCounterPlayerLimitPacket;
import pl.rosehc.platform.PlatformPlugin;

public final class SetMaskCommand {

  private final PlatformPlugin plugin;

  public SetMaskCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-setmask")
  @Command(value = {"setmask",
      "mask"}, description = "Zmienia ilość graczy, od której maska ma być aktywna")
  public void handleSetMask(final @Sender CommandSender sender,
      final @Name("limit (liczba mniejsza niż 1 = off)") int limit) {
    ChatHelper.sendMessage(sender,
        limit < 1 ? this.plugin.getPlatformConfiguration().messagesWrapper.maskSuccessfullyDisabled
            : this.plugin.getPlatformConfiguration().messagesWrapper.maskSuccessfullySet.replace(
                "{LIMIT}", String.valueOf(limit)));
    this.plugin.getPlatformConfiguration().proxyMotdWrapper.counterPlayersLimit = limit;
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformSetMotdCounterPlayerLimitPacket(limit), "rhc_master_controller",
            "rhc_platform");
  }
}
