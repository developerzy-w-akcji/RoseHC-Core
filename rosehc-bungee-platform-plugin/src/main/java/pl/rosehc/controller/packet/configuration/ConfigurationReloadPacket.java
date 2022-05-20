package pl.rosehc.controller.packet.configuration;

import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class ConfigurationReloadPacket extends Packet {

  @Override
  public void handle(final PacketHandler ignored) {
  }
}
