package pl.rosehc.controller.packet.auth;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class AuthInitializationRequestPacket extends CallbackPacket {

  private int proxyIdentifier;

  private AuthInitializationRequestPacket() {
  }

  public AuthInitializationRequestPacket(final int proxyIdentifier) {
    this.proxyIdentifier = proxyIdentifier;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public int getProxyIdentifier() {
    return this.proxyIdentifier;
  }
}
