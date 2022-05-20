package pl.rosehc.controller.packet.auth;

import java.util.UUID;
import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class AuthQueueAddPacket extends Packet {

  private UUID uniqueId;
  private String sectorName;
  private int priority, proxyIdentifier;

  private AuthQueueAddPacket() {
  }

  public AuthQueueAddPacket(final UUID uniqueId, final String sectorName, final int priority,
      final int proxyIdentifier) {
    this.uniqueId = uniqueId;
    this.sectorName = sectorName;
    this.priority = priority;
    this.proxyIdentifier = proxyIdentifier;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public String getSectorName() {
    return this.sectorName;
  }

  public int getPriority() {
    return this.priority;
  }

  public int getProxyIdentifier() {
    return this.proxyIdentifier;
  }
}
