package pl.rosehc.controller.packet.platform.ban;

import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.PlatformPacketHandler;

public final class PlatformBanBroadcastPacket extends Packet {

  private String broadcastMessage;
  private boolean silent;

  private PlatformBanBroadcastPacket() {
  }

  public PlatformBanBroadcastPacket(final String broadcastMessage, final boolean silent) {
    this.broadcastMessage = broadcastMessage;
    this.silent = silent;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((PlatformPacketHandler) handler).handle(this);
  }

  public String getBroadcastMessage() {
    return this.broadcastMessage;
  }

  public boolean isSilent() {
    return this.silent;
  }
}
