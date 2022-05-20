package pl.rosehc.randomtp.system;

import java.util.Arrays;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.randomtp.RandomTPConfigurationRequestPacket;
import pl.rosehc.controller.packet.randomtp.RandomTPConfigurationResponsePacket;
import pl.rosehc.randomtp.AbstractRandomTPPlugin;
import pl.rosehc.randomtp.RandomTPPlugin;
import pl.rosehc.randomtp.system.arena.Arena;
import pl.rosehc.randomtp.system.arena.ArenaDisconnectGhostPlayersTask;
import pl.rosehc.randomtp.system.arena.ArenaFactory;
import pl.rosehc.randomtp.system.arena.ArenaUpdateTask;
import pl.rosehc.randomtp.system.listeners.PlayerBlockBreakListener;
import pl.rosehc.randomtp.system.listeners.PlayerBlockPlaceListener;
import pl.rosehc.randomtp.system.listeners.PlayerBucketEmptyListener;
import pl.rosehc.randomtp.system.listeners.PlayerBucketFillListener;
import pl.rosehc.randomtp.system.listeners.PlayerCommandPreprocessListener;
import pl.rosehc.randomtp.system.listeners.PlayerDamageByEntityListener;
import pl.rosehc.randomtp.system.listeners.PlayerJoinListener;
import pl.rosehc.randomtp.system.listeners.PlayerQuitListener;
import pl.rosehc.randomtp.system.listeners.PlayerTeleportListener;
import pl.rosehc.randomtp.system.packet.SystemPacketHandler;
import pl.rosehc.randomtp.system.packet.SystemRandomTPArenaCreateRequestPacket;
import pl.rosehc.sectors.sector.Sector;

public final class SystemRandomTPPlugin extends AbstractRandomTPPlugin {

  private static SystemRandomTPPlugin instance;
  private SystemRandomTPConfiguration randomTPConfiguration;
  private ArenaFactory arenaFactory;

  public SystemRandomTPPlugin(final RandomTPPlugin original) {
    super(original);
  }

  public static SystemRandomTPPlugin getInstance() {
    return instance;
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Sector sector,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new SystemPacketHandler(this), Arrays.asList(
          "rhc_global",
          "rhc_rtp_" + sector.getName()
      ), Arrays.asList(
          ConfigurationSynchronizePacket.class,
          RandomTPConfigurationResponsePacket.class,
          SystemRandomTPArenaCreateRequestPacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new RandomTPConfigurationRequestPacket(sector.getName(), false),
              new Callback() {

                @Override
                public void done(final CallbackPacket packet) {
                  final RandomTPConfigurationResponsePacket responsePacket = (RandomTPConfigurationResponsePacket) packet;
                  instance = SystemRandomTPPlugin.this;
                  randomTPConfiguration = ConfigurationHelper.deserializeConfiguration(
                      responsePacket.getSerializedConfigurationData(),
                      SystemRandomTPConfiguration.class);
                  randomTPConfiguration.parsedCreationIdleTime = TimeHelper.timeFromString(
                      randomTPConfiguration.creationIdleTime);
                  randomTPConfiguration.parsedDeletionTime = TimeHelper.timeFromString(
                      randomTPConfiguration.deletionTime);
                  arenaFactory = new ArenaFactory();
                  getServer().getPluginManager()
                      .registerEvents(new PlayerJoinListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerQuitListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerBlockBreakListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerBlockPlaceListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerBucketEmptyListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerBucketFillListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager().registerEvents(
                      new PlayerCommandPreprocessListener(SystemRandomTPPlugin.this),
                      SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerDamageByEntityListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  getServer().getPluginManager()
                      .registerEvents(new PlayerTeleportListener(SystemRandomTPPlugin.this),
                          SystemRandomTPPlugin.this.getOriginal());
                  new ArenaDisconnectGhostPlayersTask(SystemRandomTPPlugin.this);
                  new ArenaUpdateTask(SystemRandomTPPlugin.this);
                }

                @Override
                public void error(final String ignored) {
                }
              }, "rhc_master_controller");
    }
  }

  @Override
  public void onDeInitialize() {
    for (final Arena arena : this.arenaFactory.getArenaMap().values()) {
      for (final Entry<String, Location> entry : arena.getPlacedBlockLocationMap().entrySet()) {
        entry.getValue().getBlock().setType(Material.AIR);
      }
    }
  }

  public SystemRandomTPConfiguration getRandomTPConfiguration() {
    return this.randomTPConfiguration;
  }

  public void setRandomTPConfiguration(final SystemRandomTPConfiguration randomTPConfiguration) {
    this.randomTPConfiguration = randomTPConfiguration;
  }

  public ArenaFactory getArenaFactory() {
    return this.arenaFactory;
  }
}
