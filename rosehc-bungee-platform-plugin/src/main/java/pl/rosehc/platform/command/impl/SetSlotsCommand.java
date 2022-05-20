package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Range;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.PlatformSetSlotsPacket;
import pl.rosehc.platform.PlatformPlugin;

public final class SetSlotsCommand {

  private final PlatformPlugin plugin;

  public SetSlotsCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-setslots")
  @Command(value = {"setslots proxy", "slots proxy",
      "slot proxy"}, description = "Ustawia sloty dla każdego proxy")
  public void handleProxySetSlots(final @Sender CommandSender sender,
      final @Name("slots") @Range(min = 1, max = 20_000) int slots) {
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformSetSlotsPacket(slots, true), "rhc_master_controller",
            "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.slotsSuccessfullySet.replace(
            "{SLOTS}", String.valueOf(slots)));
  }

  @Permission("platform-command-setslots")
  @Command(value = {"setslots sectors", "slots sectors",
      "slot sectors"}, description = "Ustawia sloty dla każdego sektora")
  public void handleSectorsSetSlots(final @Sender CommandSender sender,
      final @Name("slots") @Range(min = 1, max = 1500) int slots) {
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformSetSlotsPacket(slots, false), "rhc_master_controller",
            "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.slotsSuccessfullySet.replace(
            "{SLOTS}", String.valueOf(slots)));
  }

  @Permission("platform-command-setslots")
  @Command(value = {"setslots", "slots", "slot"}, description = "Wyświetla użycie slotów.")
  public void handleSetSlotsUsage(final @Sender CommandSender sender) {
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.slotsUsage);
  }
}
