package pl.rosehc.controller.packet.achievements;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class AchievementsConfigurationRequestPacket extends CallbackPacket {

  private String sectorName;

  private AchievementsConfigurationRequestPacket() {
  }

  public AchievementsConfigurationRequestPacket(final String sectorName) {
    this.sectorName = sectorName;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }
}
