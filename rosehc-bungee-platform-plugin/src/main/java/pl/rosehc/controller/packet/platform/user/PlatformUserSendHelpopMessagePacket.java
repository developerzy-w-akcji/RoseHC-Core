package pl.rosehc.controller.packet.platform.user;

import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.PlatformPacketHandler;

@SuppressWarnings("SpellCheckingInspection")
public final class PlatformUserSendHelpopMessagePacket extends Packet {

  private String playerName, sectorName, message;
  private int proxyIdentifier;

  private PlatformUserSendHelpopMessagePacket() {
  }

  public PlatformUserSendHelpopMessagePacket(final String playerName, final String sectorName,
      final String message, final int proxyIdentifier) {
    this.playerName = playerName;
    this.sectorName = sectorName;
    this.message = message;
    this.proxyIdentifier = proxyIdentifier;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((PlatformPacketHandler) handler).handle(this);
  }

  public String getPlayerName() {
    return this.playerName;
  }

  public String getSectorName() {
    return this.sectorName;
  }

  public String getMessage() {
    return this.message;
  }

  public int getProxyIdentifier() {
    return this.proxyIdentifier;
  }
}
