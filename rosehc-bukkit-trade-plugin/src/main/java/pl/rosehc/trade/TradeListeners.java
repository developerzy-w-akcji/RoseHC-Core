package pl.rosehc.trade;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import pl.rosehc.adapter.helper.ChatHelper;

public final class TradeListeners implements Listener {

  private static final Cache<UUID, List<UUID>> TRADE_REQUEST_CACHE = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofSeconds(30L)).build();
  private final TradePlugin plugin;

  public TradeListeners(final TradePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteractAtEntity(final PlayerInteractEntityEvent event) {
    final Player senderPlayer = event.getPlayer();
    if (event.getRightClicked() instanceof Player && senderPlayer.isSneaking()) {
      final Player receiverPlayer = (Player) event.getRightClicked();
      final List<UUID> receiverTradeRequestList = TRADE_REQUEST_CACHE.getIfPresent(
          receiverPlayer.getUniqueId());
      if (Objects.nonNull(receiverTradeRequestList)) {
        for (final UUID uuid : new ArrayList<>(receiverTradeRequestList)) {
          if (uuid.equals(senderPlayer.getUniqueId())) {
            receiverTradeRequestList.remove(uuid);
            if (receiverTradeRequestList.isEmpty()) {
              TRADE_REQUEST_CACHE.invalidate(receiverPlayer.getUniqueId());
            }

            if (this.plugin.getTradeFactory().findTrade(receiverPlayer.getUniqueId()).isPresent()) {
              ChatHelper.sendMessage(receiverPlayer,
                  this.plugin.getTradeConfiguration().youAreCurrentlyInTrade);
              return;
            }

            if (this.plugin.getTradeFactory().findTrade(senderPlayer.getUniqueId()).isPresent()) {
              ChatHelper.sendMessage(receiverPlayer,
                  this.plugin.getTradeConfiguration().requestSentSender);
              return;
            }

            final Trade trade = new Trade(senderPlayer, receiverPlayer,
                TradeInventoryHelper.create(senderPlayer),
                TradeInventoryHelper.create(receiverPlayer));
            this.plugin.getTradeFactory().addTrade(trade);
            senderPlayer.openInventory(trade.getSenderInventory());
            receiverPlayer.openInventory(trade.getReceiverInventory());
            return;
          }
        }
      }

      List<UUID> senderTradeRequestList = TRADE_REQUEST_CACHE.getIfPresent(
          senderPlayer.getUniqueId());
      if (Objects.isNull(senderTradeRequestList)) {
        senderTradeRequestList = new ArrayList<>();
        TRADE_REQUEST_CACHE.put(senderPlayer.getUniqueId(), senderTradeRequestList);
      }

      if (senderTradeRequestList.contains(receiverPlayer.getUniqueId())) {
        ChatHelper.sendMessage(senderPlayer,
            this.plugin.getTradeConfiguration().requestAlreadySent);
        return;
      }

      senderTradeRequestList.add(receiverPlayer.getUniqueId());
      ChatHelper.sendMessage(senderPlayer,
          this.plugin.getTradeConfiguration().requestSentSender.replace("{PLAYER_NAME}",
              receiverPlayer.getName()));
      ChatHelper.sendMessage(receiverPlayer,
          this.plugin.getTradeConfiguration().requestSentReceiver.replace("{PLAYER_NAME}",
              senderPlayer.getName()));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClick(final InventoryClickEvent event) {
    final Inventory inventory = event.getInventory();
    final Inventory clickedInventory = event.getClickedInventory();
    if ((Objects.nonNull(inventory) && inventory.getHolder() instanceof TradeInventoryHolder
        && event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) || (
        Objects.nonNull(clickedInventory)
            && clickedInventory.getHolder() instanceof TradeInventoryHolder)) {
      ((TradeInventoryHolder) ((Objects.isNull(inventory) ? clickedInventory
          : inventory)).getHolder()).handleClick(event);
    }
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    TRADE_REQUEST_CACHE.invalidate(event.getPlayer().getUniqueId());
    this.plugin.getTradeFactory().findTrade(event.getPlayer().getUniqueId())
        .ifPresent(trade -> trade.cancel(true, false, false));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDrag(final InventoryDragEvent event) {
    if (event.getInventory().getHolder() instanceof TradeInventoryHolder) {
      ((TradeInventoryHolder) event.getInventory().getHolder()).handleDrag(event);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onClose(final InventoryCloseEvent event) {
    if (event.getInventory().getHolder() instanceof TradeInventoryHolder) {
      ((TradeInventoryHolder) event.getInventory().getHolder()).handleClose(event);
    }
  }
}
