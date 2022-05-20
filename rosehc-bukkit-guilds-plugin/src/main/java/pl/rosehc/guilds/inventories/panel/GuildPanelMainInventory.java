package pl.rosehc.guilds.inventories.panel;

import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildPanelMainInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildPanelMainInventory(final Player player, final Guild guild) {
    this.player = player;
    final SpigotGuiWrapper guildPanelMainInventoryWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("panel_main");
    if (guildPanelMainInventoryWrapper == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = new BukkitInventory(
        ChatHelper.colored(guildPanelMainInventoryWrapper.inventoryName),
        guildPanelMainInventoryWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildPanelMainInventoryWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
