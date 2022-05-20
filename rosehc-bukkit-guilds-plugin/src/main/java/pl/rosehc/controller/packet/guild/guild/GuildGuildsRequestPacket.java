package pl.rosehc.controller.packet.guild.guild;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class GuildGuildsRequestPacket extends CallbackPacket {

  private String sectorName;

  private GuildGuildsRequestPacket() {
  }

  public GuildGuildsRequestPacket(final String sectorName) {
    this.sectorName = sectorName;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public String getSectorName() {
    return this.sectorName;
  }
}
