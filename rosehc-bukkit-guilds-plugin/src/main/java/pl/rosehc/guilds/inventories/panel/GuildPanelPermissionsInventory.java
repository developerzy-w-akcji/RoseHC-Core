package pl.rosehc.guilds.inventories.panel;

import java.util.Map.Entry;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildPanelPermissionsInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildPanelPermissionsInventory(final Player player, final Guild guild) {
    this.player = player;
    final SpigotGuiWrapper guildPermissionsInventoryWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("panel_permissions");
    if (guildPermissionsInventoryWrapper == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = new BukkitInventory(
        ChatHelper.colored(guildPermissionsInventoryWrapper.inventoryName),
        guildPermissionsInventoryWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildPermissionsInventoryWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : guildPermissionsInventoryWrapper.elements.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("rank_assigning") && !entry.getKey()
          .equalsIgnoreCase("rank_editing") && !entry.getKey().equalsIgnoreCase("member_editing")) {
        this.inventory.setElement(entry.getValue().slot,
            new BukkitInventoryElement(entry.getValue().asItemStack()));
      }
    }

    final SpigotGuiElementWrapper rankAssigningElementWrapper = guildPermissionsInventoryWrapper.elements.get(
        "rank_assigning");
    if (rankAssigningElementWrapper instanceof DefaultSpigotGuiElementWrapper) {
      final DefaultSpigotGuiElementWrapper rankAssigningElement = (DefaultSpigotGuiElementWrapper) rankAssigningElementWrapper;
      this.inventory.setElement(rankAssigningElement.slot,
          new BukkitInventoryElement(rankAssigningElement.asItemStack(), event -> {

          }));
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
