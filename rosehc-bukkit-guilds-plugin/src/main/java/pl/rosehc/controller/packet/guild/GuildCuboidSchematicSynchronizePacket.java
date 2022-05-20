package pl.rosehc.controller.packet.guild;

import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.GuildPacketHandler;

public final class GuildCuboidSchematicSynchronizePacket extends Packet {

  private byte[] schematicData;

  private GuildCuboidSchematicSynchronizePacket() {
  }

  public GuildCuboidSchematicSynchronizePacket(final byte[] schematicData) {
    this.schematicData = schematicData;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((GuildPacketHandler) handler).handle(this);
  }

  public byte[] getSchematicData() {
    return this.schematicData;
  }
}
