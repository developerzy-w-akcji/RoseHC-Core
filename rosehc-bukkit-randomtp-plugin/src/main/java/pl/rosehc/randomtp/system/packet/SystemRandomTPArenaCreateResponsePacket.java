package pl.rosehc.randomtp.system.packet;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class SystemRandomTPArenaCreateResponsePacket extends CallbackPacket {

  private String centerLocation;

  private SystemRandomTPArenaCreateResponsePacket() {
  }

  public SystemRandomTPArenaCreateResponsePacket(final String centerLocation) {
    this.centerLocation = centerLocation;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public String getCenterLocation() {
    return this.centerLocation;
  }
}
