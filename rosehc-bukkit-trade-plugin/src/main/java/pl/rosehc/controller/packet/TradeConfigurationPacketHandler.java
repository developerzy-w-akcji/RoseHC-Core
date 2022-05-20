package pl.rosehc.controller.packet;

import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.trade.TradeConfiguration;
import pl.rosehc.trade.TradePlugin;

public final class TradeConfigurationPacketHandler implements PacketHandler,
    ConfigurationSynchronizePacketHandler {

  private final TradePlugin plugin;

  public TradeConfigurationPacketHandler(final TradePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName()
        .equals("pl.rosehc.controller.configuration.impl.configuration.TradeConfiguration")) {
      final TradeConfiguration configuration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), TradeConfiguration.class);
      this.plugin.setTradeConfiguration(configuration);
    }
  }
}
