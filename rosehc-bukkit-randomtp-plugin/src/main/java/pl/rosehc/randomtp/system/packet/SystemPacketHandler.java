package pl.rosehc.randomtp.system.packet;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.ConfigurationSynchronizePacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.randomtp.system.SystemRandomTPConfiguration;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.randomtp.system.arena.Arena;
import pl.rosehc.sectors.SectorsPlugin;

public final class SystemPacketHandler implements ConfigurationSynchronizePacketHandler,
    PacketHandler {

  private final SystemRandomTPPlugin plugin;

  public SystemPacketHandler(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName().equals(
        "pl.rosehc.controller.configuration.impl.configuration.SystemRandomTPConfiguration")) {
      final SystemRandomTPConfiguration randomTPConfiguration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), SystemRandomTPConfiguration.class);
      randomTPConfiguration.parsedCreationIdleTime = TimeHelper.timeFromString(
          randomTPConfiguration.creationIdleTime);
      randomTPConfiguration.parsedDeletionTime = TimeHelper.timeFromString(
          randomTPConfiguration.deletionTime);
      this.plugin.setRandomTPConfiguration(randomTPConfiguration);
    }
  }

  public void handle(final SystemRandomTPArenaCreateRequestPacket packet) {
    if (!SectorsPlugin.getInstance().isLoaded()) {
      return;
    }

    this.newLocation().whenComplete((location, error) -> {
      final SystemRandomTPArenaCreateResponsePacket responsePacket = new SystemRandomTPArenaCreateResponsePacket(
          Objects.nonNull(location) ? SerializeHelper.serializeLocation(location) : null);
      responsePacket.setCallbackId(packet.getCallbackId());
      responsePacket.setResponse(true);
      if (error != null) {
        this.plugin.getRedisAdapter()
            .sendPacket(responsePacket, "rhc_rtp_" + packet.getSectorName());
        return;
      }

      final Arena arena = new Arena(packet.getSenderPlayerUniqueId(),
          packet.getNearestPlayerUniqueId(), location);
      this.plugin.getArenaFactory().addArena(arena);
      responsePacket.setSuccess(true);
      this.plugin.getRedisAdapter().sendPacket(responsePacket, "rhc_rtp_" + packet.getSectorName());
    });
  }

  private CompletableFuture<Location> newLocation() {
    final CompletableFuture<Location> future = new CompletableFuture<>();
    this.plugin.getServer().getScheduler()
        .scheduleSyncDelayedTask(this.plugin.getOriginal(), () -> {
          for (int tries = 0; tries < 16; tries++) {
            final Location randomLocation = SectorsPlugin.getInstance().getSectorFactory()
                .getCurrentSector().random();
            if (!this.plugin.getArenaFactory().canCreateArena(randomLocation)) {
              if (tries == 15) {
                future.completeExceptionally(
                    new UnsupportedOperationException("Nie można było stworzyć areny!"));
                break;
              }

              continue;
            }

            future.complete(randomLocation);
          }
        });
    return future;
  }
}
