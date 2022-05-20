package pl.rosehc.guilds.inventories;

import java.util.Map.Entry;
import java.util.function.Consumer;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.GuildType;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildTypeSelectionInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildTypeSelectionInventory(final Player player, final Consumer<GuildType> selectAction) {
    this.player = player;
    final SpigotGuiWrapper guildTypeSelectionGuiWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("type_selection");
    if (guildTypeSelectionGuiWrapper == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = guildTypeSelectionGuiWrapper.inventoryType != null ? new BukkitInventory(
        ChatHelper.colored(guildTypeSelectionGuiWrapper.inventoryName),
        InventoryType.valueOf(guildTypeSelectionGuiWrapper.inventoryType))
        : new BukkitInventory(ChatHelper.colored(guildTypeSelectionGuiWrapper.inventoryName),
            guildTypeSelectionGuiWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildTypeSelectionGuiWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : guildTypeSelectionGuiWrapper.elements.entrySet()) {
      boolean wasEnumConstantFound;
      try {
        GuildType.valueOf(entry.getKey());
        wasEnumConstantFound = true;
      } catch (final Exception ignored) {
        wasEnumConstantFound = false;
      }

      if (!wasEnumConstantFound) {
        this.inventory.setElement(entry.getValue().slot,
            new BukkitInventoryElement(entry.getValue().asItemStack()));
      }
    }

    for (final GuildType type : GuildType.values()) {
      final SpigotGuiElementWrapper element = guildTypeSelectionGuiWrapper.elements.get(
          type.name());
      if (element instanceof DefaultSpigotGuiElementWrapper) {
        this.inventory.setElement(element.slot,
            new BukkitInventoryElement(element.asItemStack(), event -> selectAction.accept(type)));
      }
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
