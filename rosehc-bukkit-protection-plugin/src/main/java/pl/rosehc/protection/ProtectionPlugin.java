package pl.rosehc.protection;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import me.vaperion.blade.Blade;
import me.vaperion.blade.container.impl.BukkitCommandContainer;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.plugin.BukkitPlugin;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.ProtectionPacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.protection.ProtectionConfigurationRequestPacket;
import pl.rosehc.controller.packet.protection.ProtectionConfigurationResponsePacket;
import pl.rosehc.protection.command.ProtectionCommand;
import pl.rosehc.protection.listener.PlayerDamageByEntityListener;
import pl.rosehc.protection.listener.PlayerPreLoginListener;
import pl.rosehc.protection.listener.PlayerQuitListener;
import pl.rosehc.protection.listener.PlayerSectorConnectingListener;
import pl.rosehc.protection.user.ProtectionUserFactory;
import pl.rosehc.protection.user.ProtectionUserRepository;
import pl.rosehc.protection.user.task.ProtectionUserBarUpdateTask;
import pl.rosehc.protection.user.task.ProtectionUserExpiryTask;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorInitializationHook;

public final class ProtectionPlugin extends BukkitPlugin implements SectorInitializationHook {

  private ProtectionConfiguration protectionConfiguration;
  private ProtectionUserRepository protectionUserRepository;
  private ProtectionUserFactory protectionUserFactory;

  @Override
  public void onLoad() {
    SectorsPlugin.getInstance().registerHook(this);
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Sector sector,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new ProtectionPacketHandler(this), Arrays.asList(
          "rhc_protection_" + sector.getName(),
          "rhc_global"
      ), Arrays.asList(
          ProtectionConfigurationResponsePacket.class,
          ConfigurationSynchronizePacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new ProtectionConfigurationRequestPacket(sector.getName()), new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              try {
                final ProtectionConfigurationResponsePacket responsePacket = (ProtectionConfigurationResponsePacket) packet;
                protectionConfiguration = ConfigurationHelper.deserializeConfiguration(
                    responsePacket.getConfigurationData(), ProtectionConfiguration.class);
                protectionUserRepository = new ProtectionUserRepository(
                    ProtectionPlugin.this.getDatabaseAdapter(), ProtectionPlugin.this);
                protectionUserFactory = new ProtectionUserFactory();
                protectionConfiguration.parsedExpiryTime = TimeHelper.timeFromString(
                    protectionConfiguration.expiryTime);

                registerListeners(new PlayerPreLoginListener(ProtectionPlugin.this),
                    new PlayerDamageByEntityListener(ProtectionPlugin.this),
                    new PlayerSectorConnectingListener(ProtectionPlugin.this),
                    new PlayerQuitListener(ProtectionPlugin.this));
                Blade.of().fallbackPrefix("protection")
                    .containerCreator(BukkitCommandContainer.CREATOR).asyncExecutor(
                        runnable -> getServer().getScheduler()
                            .runTaskAsynchronously(ProtectionPlugin.this, runnable)).build()
                    .register(new ProtectionCommand(ProtectionPlugin.this));
                new ProtectionUserExpiryTask(ProtectionPlugin.this);
                new ProtectionUserBarUpdateTask(ProtectionPlugin.this);
                completionStage.removeWaiter(waiter);
              } catch (final SQLException ex) {
                getLogger().log(Level.SEVERE, "Plugin nie mógł zostać poprawnie załadowany!", ex);
                getServer().getPluginManager().disablePlugin(ProtectionPlugin.this);
              }
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  public ProtectionConfiguration getProtectionConfiguration() {
    return this.protectionConfiguration;
  }

  public void setProtectionConfiguration(final ProtectionConfiguration protectionConfiguration) {
    this.protectionConfiguration = protectionConfiguration;
  }

  public ProtectionUserRepository getProtectionUserRepository() {
    return this.protectionUserRepository;
  }

  public ProtectionUserFactory getProtectionUserFactory() {
    return this.protectionUserFactory;
  }
}
