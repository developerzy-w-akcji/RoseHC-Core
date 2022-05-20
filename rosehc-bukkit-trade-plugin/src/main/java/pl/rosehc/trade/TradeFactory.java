package pl.rosehc.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TradeFactory {

  private final Map<UUID, Trade> tradeMap = new HashMap<>();

  public void addTrade(final Trade trade) {
    this.tradeMap.put(trade.getSender().getUniqueId(), trade);
    this.tradeMap.put(trade.getReceiver().getUniqueId(), trade);
  }

  public void removeTrade(final Trade trade) {
    this.tradeMap.remove(trade.getReceiver().getUniqueId());
    this.tradeMap.remove(trade.getSender().getUniqueId());
  }

  public Optional<Trade> findTrade(final UUID uniqueId) {
    return Optional.ofNullable(this.tradeMap.get(uniqueId));
  }
}
