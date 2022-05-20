package pl.rosehc.trade;

import java.util.Arrays;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.plugin.BukkitPlugin;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.TradeConfigurationPacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.trade.TradeConfigurationRequestPacket;
import pl.rosehc.controller.packet.trade.TradeConfigurationResponsePacket;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorInitializationHook;

public final class TradePlugin extends BukkitPlugin implements SectorInitializationHook {

  private static TradePlugin instance;
  private TradeConfiguration tradeConfiguration;
  private TradeFactory tradeFactory;

  public static TradePlugin getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
    SectorsPlugin.getInstance().registerHook(this);
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Sector sector,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new TradeConfigurationPacketHandler(this), Arrays.asList(
          "rhc_trade_" + sector.getName(),
          "rhc_global"
      ), Arrays.asList(
          TradeConfigurationResponsePacket.class,
          ConfigurationSynchronizePacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new TradeConfigurationRequestPacket(sector.getName()), new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              final TradeConfigurationResponsePacket responsePacket = (TradeConfigurationResponsePacket) packet;
              tradeConfiguration = ConfigurationHelper.deserializeConfiguration(
                  responsePacket.getConfigurationData(), TradeConfiguration.class);
              tradeFactory = new TradeFactory();
              registerListeners(new TradeListeners(TradePlugin.this));
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  public TradeConfiguration getTradeConfiguration() {
    return this.tradeConfiguration;
  }

  public void setTradeConfiguration(final TradeConfiguration tradeConfiguration) {
    this.tradeConfiguration = tradeConfiguration;
  }

  public TradeFactory getTradeFactory() {
    return this.tradeFactory;
  }
}
