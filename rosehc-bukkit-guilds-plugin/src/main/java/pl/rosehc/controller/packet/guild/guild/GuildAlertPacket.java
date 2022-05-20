package pl.rosehc.controller.packet.guild.guild;

import pl.rosehc.adapter.redis.packet.Packet;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.GuildPacketHandler;

public final class GuildAlertPacket extends Packet {

  private String guildTag, message;

  private GuildAlertPacket() {
  }

  public GuildAlertPacket(final String guildTag, final String message) {
    this.guildTag = guildTag;
    this.message = message;
  }

  @Override
  public void handle(final PacketHandler handler) {
    ((GuildPacketHandler) handler).handle(this);
  }

  public String getGuildTag() {
    return this.guildTag;
  }

  public String getMessage() {
    return this.message;
  }
}
