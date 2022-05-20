package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.configuration.ConfigurationReloadPacket;
import pl.rosehc.platform.PlatformPlugin;

public final class ConfigReloadCommand {

  private final PlatformPlugin plugin;

  public ConfigReloadCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-cfgreload")
  @Command(value = {"configreload",
      "cfgreload"}, async = true, description = "Przeładowuje konfigurację.")
  public void handleConfigReload(final @Sender CommandSender sender) {
    this.plugin.getRedisAdapter()
        .sendPacket(new ConfigurationReloadPacket(), "rhc_master_controller");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.configReloadRequested);
  }
}
