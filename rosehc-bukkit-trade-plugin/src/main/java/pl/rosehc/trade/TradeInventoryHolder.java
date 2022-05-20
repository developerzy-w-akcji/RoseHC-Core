package pl.rosehc.trade;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class TradeInventoryHolder implements InventoryHolder {

  private final TradePlugin plugin;
  private Inventory inventory;

  public TradeInventoryHolder(final TradePlugin plugin) {
    this.plugin = plugin;
  }

  public void handleClick(final InventoryClickEvent event) {
    if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
      event.setCancelled(true);
      event.setResult(Result.DENY);
      return;
    }

    final Player player = (Player) event.getWhoClicked();
    this.plugin.getTradeFactory().findTrade(player.getUniqueId()).ifPresent(trade -> {
      final int rawSlot = event.getRawSlot();
      if (rawSlot == this.plugin.getTradeConfiguration().tradeGuiWrapper.elements.get(
          "accept_first").slot) {
        event.setCancelled(true);
        event.setResult(Result.DENY);
        trade.setAccepted(player);
        return;
      }

      if (trade.isAccepted(player) || (
          !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
              && !this.plugin.getTradeConfiguration().leftSlots.contains(rawSlot))) {
        event.setCancelled(true);
        event.setResult(Result.DENY);
        return;
      }

      if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
          final ItemStack currentItem = event.getCurrentItem();
          if (Objects.nonNull(currentItem)) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            int freeSlot = -1;
            for (final int leftSlot : this.plugin.getTradeConfiguration().leftSlots) {
              if (Objects.isNull(this.inventory.getItem(leftSlot))) {
                freeSlot = leftSlot;
                break;
              }
            }

            if (freeSlot != -1) {
              event.getView().getBottomInventory().setItem(event.getSlot(), null);
              this.inventory.setItem(freeSlot, currentItem);
            }
          }
        } else if (!this.plugin.getTradeConfiguration().leftSlots.contains(rawSlot)) {
          event.setCancelled(true);
          event.setResult(Result.DENY);
          return;
        }
      }

      this.plugin.getServer().getScheduler()
          .scheduleSyncDelayedTask(this.plugin, () -> trade.syncInventory(this.inventory));
    });
  }

  public void handleDrag(final InventoryDragEvent event) {
    final Player player = (Player) event.getWhoClicked();
    this.plugin.getTradeFactory().findTrade(player.getUniqueId()).ifPresent(trade -> {
      for (final int rawSlot : event.getRawSlots()) {
        if (!this.plugin.getTradeConfiguration().leftSlots.contains(rawSlot)) {
          event.setCancelled(true);
          event.setResult(Result.DENY);
          break;
        }
      }

      this.plugin.getServer().getScheduler()
          .scheduleSyncDelayedTask(this.plugin, () -> trade.syncInventory(this.inventory));
    });
  }

  public void handleClose(final InventoryCloseEvent event) {
    final Player player = (Player) event.getPlayer();
    this.plugin.getTradeFactory().findTrade(player.getUniqueId())
        .filter(trade -> !trade.isFinished()).ifPresent(trade -> {
          trade.cancel(true, false, true);
          if (trade.getSender().getUniqueId().equals(player.getUniqueId())) {
            trade.getReceiver().closeInventory();
          } else {
            trade.getSender().closeInventory();
          }

          this.plugin.getTradeFactory().removeTrade(trade);
        });
  }

  @Override
  public Inventory getInventory() {
    return this.inventory;
  }

  public void setInventory(final Inventory inventory) {
    this.inventory = inventory;
  }
}
