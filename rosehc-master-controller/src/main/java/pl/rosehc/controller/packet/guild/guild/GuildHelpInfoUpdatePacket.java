package pl.rosehc.controller.packet.guild.guild;

import java.util.UUID;
import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.GuildPacketHandler;

public final class GuildHelpInfoUpdatePacket extends Packet {

  private String guildTag;
  private UUID uniqueId;
  private boolean isAlly;
  private int x, y, z;

  private GuildHelpInfoUpdatePacket() {
  }

  public GuildHelpInfoUpdatePacket(final String guildTag, final UUID uniqueId, final boolean isAlly,
      final int x, final int y, final int z) {
    this.guildTag = guildTag;
    this.uniqueId = uniqueId;
    this.x = x;
    this.y = y;
    this.z = z;
    this.isAlly = isAlly;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((GuildPacketHandler) handler).handle(this);
  }

  public String getGuildTag() {
    return this.guildTag;
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public boolean isAlly() {
    return this.isAlly;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getZ() {
    return this.z;
  }
}
