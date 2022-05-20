package pl.rosehc.controller.packet.randomtp;

import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.adapter.redis.packet.PacketHandler;

public final class RandomTPConfigurationRequestPacket extends CallbackPacket {

  private String sectorName;
  private boolean linker;

  private RandomTPConfigurationRequestPacket() {
  }

  public RandomTPConfigurationRequestPacket(final String sectorName, final boolean linker) {
    this.sectorName = sectorName;
    this.linker = linker;
  }

  @Override
  public void handle(final PacketHandler ignored) {
  }

  public String getSectorName() {
    return this.sectorName;
  }

  public boolean isLinker() {
    return this.linker;
  }
}
