package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Range;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.PlatformMotdSettingsSynchronizePacket;
import pl.rosehc.platform.PlatformConfiguration.ProxyMotdWrapper;
import pl.rosehc.platform.PlatformPlugin;

public final class SetMotdCommand {

  private final PlatformPlugin plugin;

  public SetMotdCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-setmotd")
  @Command(value = {"setmotd",
      "motd"}, description = "Ustawia daną linijkę w motd dla każdego proxy")
  public void handleProxySetSlots(final @Sender CommandSender sender,
      final @Name("line (1-3)") @Range(min = 1, max = 3) int line,
      final @Name("spacing") int spacing, final @Name("text") @Combined String text) {
    final ProxyMotdWrapper proxyMotdWrapper = this.plugin.getPlatformConfiguration().proxyMotdWrapper;
    if (line == 3) {
      if (spacing < 1) {
        throw new BladeExitMessage("No spacing was given.");
      }

      proxyMotdWrapper.thirdLine = text;
      proxyMotdWrapper.thirdLineSpacing = spacing;
    } else if (line == 2) {
      proxyMotdWrapper.secondLine = text;
    } else {
      proxyMotdWrapper.firstLine = text;
    }

    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.motdLineSuccessfullyChanged.replace(
            "{LINE_NUMBER}", String.valueOf(line)).replace("{LINE_TEXT}", text));
    this.plugin.getRedisAdapter().sendPacket(
        new PlatformMotdSettingsSynchronizePacket(proxyMotdWrapper.firstLine,
            proxyMotdWrapper.secondLine, proxyMotdWrapper.thirdLine,
            proxyMotdWrapper.thirdLineSpacing), "rhc_master_controller", "rhc_platform");
  }
}
