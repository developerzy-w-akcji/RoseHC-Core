package pl.rosehc.randomtp.system.packet;

import java.util.UUID;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class SystemRandomTPArenaCreateRequestPacket extends CallbackPacket {

  private UUID senderPlayerUniqueId, nearestPlayerUniqueId;
  private String sectorName;

  private SystemRandomTPArenaCreateRequestPacket() {
  }

  public SystemRandomTPArenaCreateRequestPacket(final UUID senderPlayerUniqueId,
      final UUID nearestPlayerUniqueId, final String sectorName) {
    this.senderPlayerUniqueId = senderPlayerUniqueId;
    this.nearestPlayerUniqueId = nearestPlayerUniqueId;
    this.sectorName = sectorName;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((SystemPacketHandler) handler).handle(this);
  }

  public UUID getSenderPlayerUniqueId() {
    return this.senderPlayerUniqueId;
  }

  public UUID getNearestPlayerUniqueId() {
    return this.nearestPlayerUniqueId;
  }

  public String getSectorName() {
    return this.sectorName;
  }
}
