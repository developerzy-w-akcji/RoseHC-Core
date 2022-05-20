package pl.rosehc.controller.packet.achievements;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class AchievementsConfigurationResponsePacket extends CallbackPacket {

  private byte[] configurationData;

  private AchievementsConfigurationResponsePacket() {
  }

  public AchievementsConfigurationResponsePacket(final byte[] configurationData) {
    this.configurationData = configurationData;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public byte[] getConfigurationData() {
    return this.configurationData;
  }
}
