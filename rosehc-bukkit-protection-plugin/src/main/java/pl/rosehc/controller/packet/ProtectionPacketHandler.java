package pl.rosehc.controller.packet;

import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.protection.ProtectionConfiguration;
import pl.rosehc.protection.ProtectionPlugin;
import pl.rosehc.sectors.SectorsPlugin;

public final class ProtectionPacketHandler implements PacketHandler,
    ConfigurationSynchronizePacketHandler {

  private final ProtectionPlugin plugin;

  public ProtectionPacketHandler(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName()
        .equals("pl.rosehc.controller.configuration.impl.configuration.ProtectionConfiguration")) {
      final ProtectionConfiguration protectionConfiguration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), ProtectionConfiguration.class);
      protectionConfiguration.parsedExpiryTime = TimeHelper.timeFromString(
          protectionConfiguration.expiryTime);
      this.plugin.setProtectionConfiguration(protectionConfiguration);
    }
  }
}
