package pl.rosehc.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.trade.TradeAcceptItemSpigotGuiElementWrapper;

public final class Trade {

  private final Player sender, receiver;
  private final Inventory senderInventory, receiverInventory;

  private BukkitTask acceptTask;
  private boolean beingAcceptedSender, beingAcceptedReceiver, finished;
  private int secondsToAccept;

  public Trade(final Player sender, final Player receiver, final Inventory senderInventory,
      final Inventory receiverInventory) {
    this.sender = sender;
    this.receiver = receiver;
    this.senderInventory = senderInventory;
    this.receiverInventory = receiverInventory;
  }

  private static List<ItemStack> fetchItemsFor(final Inventory inventory) {
    final List<ItemStack> itemStackList = new ArrayList<>();
    for (final int leftSlot : TradePlugin.getInstance().getTradeConfiguration().leftSlots) {
      final ItemStack item = inventory.getItem(leftSlot);
      if (Objects.nonNull(item) && item.getType() != Material.AIR) {
        itemStackList.add(item);
      }
    }

    return itemStackList;
  }

  public Player getSender() {
    return this.sender;
  }

  public Player getReceiver() {
    return this.receiver;
  }

  public Inventory getSenderInventory() {
    return this.senderInventory;
  }

  public Inventory getReceiverInventory() {
    return this.receiverInventory;
  }

  public boolean isAccepted(final Player player) {
    return !this.sender.getUniqueId().equals(player.getUniqueId()) ? this.beingAcceptedReceiver
        : this.beingAcceptedSender;
  }

  public boolean isFinished() {
    return this.finished;
  }

  public void setAccepted(final Player player) {
    if (!this.sender.getUniqueId().equals(player.getUniqueId())) {
      this.beingAcceptedReceiver = !this.beingAcceptedReceiver;
      this.setAcceptItemOnRightSide(true, this.beingAcceptedReceiver && !this.beingAcceptedSender);
    } else {
      this.beingAcceptedSender = !this.beingAcceptedSender;
      this.setAcceptItemOnRightSide(false, !this.beingAcceptedReceiver && this.beingAcceptedSender);
    }

    if (this.beingAcceptedSender || this.beingAcceptedReceiver) {
      if (Objects.nonNull(this.acceptTask)) {
        this.acceptTask.cancel();
      }

      this.secondsToAccept = TradePlugin.getInstance().getTradeConfiguration().secondsToAccept;
      if (this.beingAcceptedSender && this.beingAcceptedReceiver) {
        this.acceptTask = Bukkit.getScheduler().runTaskTimer(TradePlugin.getInstance(), () -> {
          if (!this.receiver.isOnline() || !this.sender.isOnline()) {
            this.cancel(true, false, false);
            TradePlugin.getInstance().getTradeFactory().removeTrade(this);
            return;
          }

          if (this.secondsToAccept <= 0) {
            this.finished = true;
            this.cancel(true, true, false);
            ChatHelper.sendMessage(this.sender,
                TradePlugin.getInstance().getTradeConfiguration().tradeCompleted.replace(
                    "{PLAYER_NAME}", this.receiver.getName()));
            ChatHelper.sendMessage(this.receiver,
                TradePlugin.getInstance().getTradeConfiguration().tradeCompleted.replace(
                    "{PLAYER_NAME}", this.sender.getName()));
            TradePlugin.getInstance().getTradeFactory().removeTrade(this);
            return;
          }

          final SpigotGuiElementWrapper firstAcceptElementWrapper = TradePlugin.getInstance()
              .getTradeConfiguration().tradeGuiWrapper.elements.get(
                  "accept_first"), secondAcceptElementWrapper = TradePlugin.getInstance()
              .getTradeConfiguration().tradeGuiWrapper.elements.get("accept_second");
          if (firstAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper) {
            final TradeAcceptItemSpigotGuiElementWrapper firstAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) firstAcceptElementWrapper;
            final ItemStack firstAcceptItemStack = new ItemStackBuilder(
                new ItemStack(Material.matchMaterial(firstAcceptElement.material),
                    this.secondsToAccept, firstAcceptElement.acceptedData)).withName(
                firstAcceptElement.acceptedName.replace("{SECONDS}",
                    String.valueOf(this.secondsToAccept))).build();
            this.senderInventory.setItem(firstAcceptElement.slot, firstAcceptItemStack);
            this.receiverInventory.setItem(firstAcceptElement.slot, firstAcceptItemStack);
          }

          if (secondAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper) {
            final TradeAcceptItemSpigotGuiElementWrapper secondAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) secondAcceptElementWrapper;
            final ItemStack firstAcceptItemStack = new ItemStackBuilder(
                new ItemStack(Material.matchMaterial(secondAcceptElement.material),
                    this.secondsToAccept, secondAcceptElement.acceptedData)).withName(
                secondAcceptElement.acceptedName.replace("{SECONDS}",
                    String.valueOf(this.secondsToAccept))).build();
            this.senderInventory.setItem(secondAcceptElement.slot, firstAcceptItemStack);
            this.receiverInventory.setItem(secondAcceptElement.slot, firstAcceptItemStack);
          }

          this.secondsToAccept--;
        }, 0L, 20L);
      }
    } else {
      this.cancel(false, false, false);
    }
  }

  public void cancel(final boolean giveBack, final boolean trade, final boolean closing) {
    if (closing && this.finished) {
      return;
    }

    if (Objects.nonNull(this.acceptTask)) {
      this.acceptTask.cancel();
      this.acceptTask = null;
    }

    this.secondsToAccept = 0;
    this.beingAcceptedSender = false;
    if (closing) {
      this.finished = true;
    }

    if (!giveBack) {
      final SpigotGuiElementWrapper firstAcceptElementWrapper = TradePlugin.getInstance()
          .getTradeConfiguration().tradeGuiWrapper.elements.get(
              "accept_first"), secondAcceptElementWrapper = TradePlugin.getInstance()
          .getTradeConfiguration().tradeGuiWrapper.elements.get("accept_second");
      if (firstAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper) {
        final TradeAcceptItemSpigotGuiElementWrapper firstAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) firstAcceptElementWrapper;
        final ItemStack firstAcceptItemStack = new ItemStackBuilder(
            new ItemStack(Material.matchMaterial(firstAcceptElement.material), 1,
                firstAcceptElement.notAcceptedData)).withName(firstAcceptElement.notAcceptedName)
            .build();
        this.senderInventory.setItem(firstAcceptElement.slot, firstAcceptItemStack);
        this.receiverInventory.setItem(firstAcceptElement.slot, firstAcceptItemStack);
      }

      if (secondAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper) {
        final TradeAcceptItemSpigotGuiElementWrapper secondAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) secondAcceptElementWrapper;
        final ItemStack firstAcceptItemStack = new ItemStackBuilder(
            new ItemStack(Material.matchMaterial(secondAcceptElement.material), 1,
                secondAcceptElement.notAcceptedData)).withName(secondAcceptElement.notAcceptedName)
            .build();
        this.senderInventory.setItem(secondAcceptElement.slot, firstAcceptItemStack);
        this.receiverInventory.setItem(secondAcceptElement.slot, firstAcceptItemStack);
      }
    } else {
      if (this.sender.isOnline()) {
        final List<ItemStack> senderItemList = fetchItemsFor(
            trade ? this.receiverInventory : this.senderInventory);
        this.sender.setItemOnCursor(new ItemStack(Material.AIR));
        if (!closing) {
          this.sender.closeInventory();
        }

        ItemHelper.addItems(this.sender, senderItemList);
      }

      if (this.receiver.isOnline()) {
        final List<ItemStack> receiverItemList = fetchItemsFor(
            trade ? this.senderInventory : this.receiverInventory);
        this.receiver.setItemOnCursor(new ItemStack(Material.AIR));
        if (!closing) {
          this.receiver.closeInventory();
        }

        ItemHelper.addItems(this.receiver, receiverItemList);
      }
    }
  }

  public void syncInventory(final Inventory sourceInventory) {
    final Inventory destinationInventory =
        sourceInventory.equals(this.senderInventory) ? this.receiverInventory
            : this.senderInventory;
    final Map<Integer, ItemStack> itemBySlotMap = new HashMap<>();
    final List<Integer> leftSlots = TradePlugin.getInstance()
        .getTradeConfiguration().leftSlots, rightSlots = TradePlugin.getInstance()
        .getTradeConfiguration().rightSlots;
    for (int i = 0; i < leftSlots.size(); i++) {
      final ItemStack item = sourceInventory.getItem(leftSlots.get(i));
      if (Objects.nonNull(item) && item.getType() != Material.AIR) {
        itemBySlotMap.put(rightSlots.get(i), item);
      }
    }

    for (final int rightSlot : rightSlots) {
      destinationInventory.clear(rightSlot);
    }
    for (final Entry<Integer, ItemStack> entry : itemBySlotMap.entrySet()) {
      destinationInventory.setItem(entry.getKey(), entry.getValue());
    }
  }

  private void setAcceptItemOnRightSide(final boolean receiver, final boolean accepted) {
    final SpigotGuiElementWrapper firstAcceptElementWrapper = TradePlugin.getInstance()
        .getTradeConfiguration().tradeGuiWrapper.elements.get(
            "accept_first"), secondAcceptElementWrapper = TradePlugin.getInstance()
        .getTradeConfiguration().tradeGuiWrapper.elements.get("accept_second");
    if (firstAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper
        && secondAcceptElementWrapper instanceof TradeAcceptItemSpigotGuiElementWrapper) {
      final TradeAcceptItemSpigotGuiElementWrapper firstAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) firstAcceptElementWrapper, secondAcceptElement = (TradeAcceptItemSpigotGuiElementWrapper) secondAcceptElementWrapper;
      final ItemStack acceptItemStack = new ItemStackBuilder(
          new ItemStack(Material.matchMaterial(firstAcceptElement.material),
              accepted ? TradePlugin.getInstance().getTradeConfiguration().secondsToAccept : 1,
              !accepted ? firstAcceptElement.notAcceptedData
                  : firstAcceptElement.acceptedData)).withName(
          accepted ? firstAcceptElement.acceptedName.replace("{SECONDS}",
              String.valueOf(TradePlugin.getInstance().getTradeConfiguration().secondsToAccept))
              : firstAcceptElement.notAcceptedName).build();
      this.senderInventory.setItem(receiver ? secondAcceptElement.slot : firstAcceptElement.slot,
          acceptItemStack);
      this.receiverInventory.setItem(receiver ? firstAcceptElement.slot : secondAcceptElement.slot,
          acceptItemStack);
    }
  }
}
