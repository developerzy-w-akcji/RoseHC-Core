package pl.rosehc.guilds.inventories;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.guild.GuildTypeWrapper;
import pl.rosehc.controller.wrapper.guild.gui.GuildItemPreviewGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper.GuildItemWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.GuildType;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildItemsInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildItemsInventory(final Player player, final GuildType type) {
    this.player = player;
    final SpigotGuiWrapper guildItemsGuiWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("guild_items");
    if (guildItemsGuiWrapper == null) {
      ChatHelper.sendMessage(player,
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound);
      throw new UnsupportedOperationException("Guild items inventory not configured.");
    }

    this.inventory = new BukkitInventory(ChatHelper.colored(guildItemsGuiWrapper.inventoryName),
        guildItemsGuiWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildItemsGuiWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : guildItemsGuiWrapper.elements.entrySet()) {
      final SpigotGuiElementWrapper element = entry.getValue();
      if (!(element instanceof GuildItemPreviewGuiElementWrapper)) {
        this.inventory.setElement(element.slot, new BukkitInventoryElement(element.asItemStack()));
      }
    }

    final double itemsPercentageChange = GuildsPlugin.getInstance()
        .getGuildsConfiguration().pluginWrapper.guildItemPercentageChangeMap.entrySet().stream()
        .filter(entry -> player.hasPermission(entry.getKey())).findFirst().map(Entry::getValue)
        .orElse(-1D);
    final List<GuildItemWrapper> itemWrapperList = GuildsPlugin.getInstance()
        .getGuildsConfiguration().pluginWrapper.guildItemWrapperMap.get(
            GuildTypeWrapper.fromOriginal(type));
    if (itemWrapperList != null && !itemWrapperList.isEmpty()) {
      for (final GuildItemWrapper wrapper : itemWrapperList) {
        final ItemStack itemStack = wrapper.asItemStack();
        int requiredAmount = itemStack.getAmount();
        if (itemsPercentageChange != -1D) {
          requiredAmount -= (int) (requiredAmount * (itemsPercentageChange / 100D));
        }

        final int inventoryAmount = ItemHelper.countItemAmount(player, itemStack);
        final String formattedPercentage = String.format("%.2f",
            inventoryAmount * 100D / requiredAmount);
      }

      for (int index = 0; index < itemWrapperList.size(); index++) {
        final SpigotGuiElementWrapper element = guildItemsGuiWrapper.elements.get("item" + index);
        if (!(element instanceof GuildItemPreviewGuiElementWrapper)) {
          continue;
        }

        final GuildItemPreviewGuiElementWrapper guildItemPreviewElement = (GuildItemPreviewGuiElementWrapper) element;
        final ItemStack itemStack = itemWrapperList.get(index).asItemStack();
        int requiredAmount = itemStack.getAmount();
        if (itemsPercentageChange != -1D) {
          requiredAmount -= (int) (requiredAmount * (itemsPercentageChange / 100D));
        }

        final int inventoryAmount = ItemHelper.countItemAmount(player, itemStack);
        final String formattedPercentage = String.format("%.2f",
            inventoryAmount * 100D / requiredAmount), formattedRequiredAmount = String.valueOf(
            requiredAmount);
        this.inventory.setElement(guildItemPreviewElement.slot, new BukkitInventoryElement(
            new ItemStackBuilder(itemStack).withName(guildItemPreviewElement.name).withLore(
                guildItemPreviewElement.lore.stream().map(
                        content -> content.replace("{REQUIRED_AMOUNT}", formattedRequiredAmount)
                            .replace("{INVENTORY_AMOUNT}", String.valueOf(inventoryAmount))
                            .replace("{AMOUNT_PERCENTAGE}", String.valueOf(formattedPercentage)))
                    .collect(Collectors.toList())).build()));
      }
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
