package pl.rosehc.trade;

import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.controller.wrapper.trade.TradeAcceptItemSpigotGuiElementWrapper;

public final class TradeInventoryHelper {

  private TradeInventoryHelper() {
  }

  public static Inventory create(final Player player) {
    final SpigotGuiWrapper tradeGuiWrapper = TradePlugin.getInstance()
        .getTradeConfiguration().tradeGuiWrapper;
    final TradeInventoryHolder holder = new TradeInventoryHolder(TradePlugin.getInstance());
    final Inventory inventory = Bukkit.createInventory(holder, tradeGuiWrapper.inventorySize,
        tradeGuiWrapper.inventoryName);
    holder.setInventory(inventory);
    for (final Entry<String, SpigotGuiElementWrapper> entry : tradeGuiWrapper.elements.entrySet()) {
      final SpigotGuiElementWrapper element = entry.getValue();
      if (element instanceof TradeAcceptItemSpigotGuiElementWrapper) {
        final TradeAcceptItemSpigotGuiElementWrapper tradeAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) element;
        inventory.setItem(element.slot, new ItemStackBuilder(
            new ItemStack(Material.matchMaterial(tradeAcceptElement.material), 1,
                tradeAcceptElement.notAcceptedData)).withName(tradeAcceptElement.notAcceptedName)
            .build());
        continue;
      }

      inventory.setItem(element.slot, element.asItemStack());
    }

    return inventory;
  }
}
