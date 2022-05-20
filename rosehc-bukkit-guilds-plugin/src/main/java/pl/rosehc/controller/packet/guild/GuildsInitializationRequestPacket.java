package pl.rosehc.controller.packet.guild;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class GuildsInitializationRequestPacket extends CallbackPacket {

  private String sectorName;

  private GuildsInitializationRequestPacket() {
  }

  public GuildsInitializationRequestPacket(final String sectorName) {
    this.sectorName = sectorName;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public String getSectorName() {
    return this.sectorName;
  }
}
