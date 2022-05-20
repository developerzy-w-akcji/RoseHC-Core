package pl.rosehc.randomtp.linker;

import java.util.Arrays;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.randomtp.RandomTPConfigurationRequestPacket;
import pl.rosehc.controller.packet.randomtp.RandomTPConfigurationResponsePacket;
import pl.rosehc.randomtp.AbstractRandomTPPlugin;
import pl.rosehc.randomtp.RandomTPPlugin;
import pl.rosehc.randomtp.linker.listeners.GroupTeleportationListener;
import pl.rosehc.randomtp.linker.listeners.SoloTeleportationListener;
import pl.rosehc.randomtp.system.packet.SystemRandomTPArenaCreateResponsePacket;
import pl.rosehc.sectors.sector.Sector;

public final class LinkerRandomTPPlugin extends AbstractRandomTPPlugin {

  private LinkerRandomTPConfiguration randomTPConfiguration;

  public LinkerRandomTPPlugin(final RandomTPPlugin original) {
    super(original);
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Sector sector,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new LinkerPacketHandler(this), Arrays.asList(
          "rhc_global",
          "rhc_rtp_" + sector.getName()
      ), Arrays.asList(
          ConfigurationSynchronizePacket.class,
          RandomTPConfigurationResponsePacket.class,
          SystemRandomTPArenaCreateResponsePacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new RandomTPConfigurationRequestPacket(sector.getName(), true),
              new Callback() {

                @Override
                public void done(final CallbackPacket packet) {
                  final RandomTPConfigurationResponsePacket responsePacket = (RandomTPConfigurationResponsePacket) packet;
                  randomTPConfiguration = ConfigurationHelper.deserializeConfiguration(
                      responsePacket.getSerializedConfigurationData(),
                      LinkerRandomTPConfiguration.class);
                  completionStage.removeWaiter(waiter);
                  getServer().getPluginManager()
                      .registerEvents(new SoloTeleportationListener(LinkerRandomTPPlugin.this),
                          original);
                  getServer().getPluginManager()
                      .registerEvents(new GroupTeleportationListener(LinkerRandomTPPlugin.this),
                          original);
                }

                @Override
                public void error(final String ignored) {
                }
              }, "rhc_master_controller");
    }
  }

  @Override
  public void onDeInitialize() {
    // Nothing to do.
  }

  public LinkerRandomTPConfiguration getRandomTPConfiguration() {
    return this.randomTPConfiguration;
  }

  public void setRandomTPConfiguration(final LinkerRandomTPConfiguration randomTPConfiguration) {
    this.randomTPConfiguration = randomTPConfiguration;
  }
}
