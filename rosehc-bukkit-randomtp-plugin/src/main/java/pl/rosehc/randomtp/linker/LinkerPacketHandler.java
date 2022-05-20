package pl.rosehc.randomtp.linker;

import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.ConfigurationSynchronizePacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;

public final class LinkerPacketHandler implements ConfigurationSynchronizePacketHandler,
    PacketHandler {

  private final LinkerRandomTPPlugin plugin;

  public LinkerPacketHandler(final LinkerRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName().equals(
        "pl.rosehc.controller.configuration.impl.configuration.LinkerRandomTPConfiguration")) {
      final LinkerRandomTPConfiguration randomTPConfiguration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), LinkerRandomTPConfiguration.class);
      this.plugin.setRandomTPConfiguration(randomTPConfiguration);
    }
  }
}
