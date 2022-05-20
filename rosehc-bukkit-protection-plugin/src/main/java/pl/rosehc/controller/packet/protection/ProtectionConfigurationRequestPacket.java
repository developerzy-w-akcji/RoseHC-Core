package pl.rosehc.controller.packet.protection;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class ProtectionConfigurationRequestPacket extends CallbackPacket {

  private String sectorName;

  private ProtectionConfigurationRequestPacket() {
  }

  public ProtectionConfigurationRequestPacket(final String sectorName) {
    this.sectorName = sectorName;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public String getSectorName() {
    return this.sectorName;
  }
}
