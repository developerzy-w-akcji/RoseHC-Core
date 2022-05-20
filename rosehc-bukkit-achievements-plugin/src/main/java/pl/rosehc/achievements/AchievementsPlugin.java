package pl.rosehc.achievements;

import java.util.Arrays;
import java.util.logging.Level;
import me.vaperion.blade.Blade;
import me.vaperion.blade.bindings.impl.BukkitBindings;
import me.vaperion.blade.container.impl.BukkitCommandContainer;
import pl.rosehc.achievements.achievement.AchievementFactory;
import pl.rosehc.achievements.commands.AchievementsCommand;
import pl.rosehc.achievements.commands.ProfileCommand;
import pl.rosehc.achievements.listener.player.PlayerJoinListener;
import pl.rosehc.achievements.listener.player.PlayerMoveListener;
import pl.rosehc.achievements.listener.player.PlayerPreLoginListener;
import pl.rosehc.achievements.listener.player.PlayerQuitListener;
import pl.rosehc.achievements.listener.player.PlayerSectorConnectingListener;
import pl.rosehc.achievements.listener.user.GuildUserUpdateKillStreakListener;
import pl.rosehc.achievements.listener.user.GuildUserUpdateKillsListener;
import pl.rosehc.achievements.listener.user.GuildUserUpdatePointsListener;
import pl.rosehc.achievements.listener.user.PlatformUserDropChanceListener;
import pl.rosehc.achievements.listener.user.PlatformUserDropLevelUpListener;
import pl.rosehc.achievements.listener.user.PlatformUserUseCustomItemListener;
import pl.rosehc.achievements.user.AchievementsUserFactory;
import pl.rosehc.achievements.user.AchievementsUserRepository;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.plugin.BukkitPlugin;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.AchievementsPacketHandler;
import pl.rosehc.controller.packet.achievements.AchievementsConfigurationRequestPacket;
import pl.rosehc.controller.packet.achievements.AchievementsConfigurationResponsePacket;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorInitializationHook;

public final class AchievementsPlugin extends BukkitPlugin implements SectorInitializationHook {

  private static AchievementsPlugin instance;
  private AchievementsConfiguration achievementsConfiguration;
  private AchievementFactory achievementFactory;
  private AchievementsUserRepository achievementsUserRepository;
  private AchievementsUserFactory achievementsUserFactory;

  public static AchievementsPlugin getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
    SectorsPlugin.getInstance().registerHook(this);
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Sector sector,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter()
          .subscribe(new AchievementsPacketHandler(AchievementsPlugin.this), Arrays.asList(
              "rhc_global",
              "rhc_achievements_" + sector.getName()
          ), Arrays.asList(
              ConfigurationSynchronizePacket.class,
              AchievementsConfigurationResponsePacket.class
          ));
      this.getRedisAdapter()
          .sendPacket(new AchievementsConfigurationRequestPacket(sector.getName()), new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              try {
                final AchievementsConfigurationResponsePacket responsePacket = (AchievementsConfigurationResponsePacket) packet;
                achievementsConfiguration = ConfigurationHelper.deserializeConfiguration(
                    responsePacket.getConfigurationData(), AchievementsConfiguration.class);
                achievementFactory = new AchievementFactory(achievementsConfiguration);
                achievementsUserRepository = new AchievementsUserRepository(getDatabaseAdapter());
                achievementsUserFactory = new AchievementsUserFactory();
                registerListeners(new PlayerPreLoginListener(AchievementsPlugin.this),
                    new PlayerJoinListener(AchievementsPlugin.this),
                    new PlayerMoveListener(AchievementsPlugin.this),
                    new PlayerSectorConnectingListener(AchievementsPlugin.this),
                    new PlayerQuitListener(AchievementsPlugin.this),
                    new PlatformUserDropChanceListener(AchievementsPlugin.this),
                    new PlatformUserDropLevelUpListener(AchievementsPlugin.this),
                    new PlatformUserUseCustomItemListener(AchievementsPlugin.this));
                try {
                  Class.forName("pl.rosehc.guilds.GuildsPlugin");
                  registerListeners(new GuildUserUpdateKillsListener(AchievementsPlugin.this),
                      new GuildUserUpdatePointsListener(AchievementsPlugin.this),
                      new GuildUserUpdateKillStreakListener(AchievementsPlugin.this));
                } catch (final Exception ignored) {
                }

                final Blade blade = Blade.of().fallbackPrefix("achievements").overrideCommands(true)
                    .executionTimeWarningThreshold(1500L)
                    .containerCreator(BukkitCommandContainer.CREATOR).binding(new BukkitBindings())
                    .asyncExecutor(runnable -> getServer().getScheduler()
                        .runTaskAsynchronously(AchievementsPlugin.this, runnable)).build();
                blade.register(new AchievementsCommand(AchievementsPlugin.this));
                blade.register(new ProfileCommand(AchievementsPlugin.this));
              } catch (final Exception ex) {
                getLogger().log(Level.SEVERE, "Plugin nie mógł zostać poprawnie załadowany!", ex);
                getServer().getPluginManager().disablePlugin(AchievementsPlugin.this);
              }
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  public AchievementsConfiguration getAchievementsConfiguration() {
    return this.achievementsConfiguration;
  }

  public void setAchievementsConfiguration(
      final AchievementsConfiguration achievementsConfiguration) {
    this.achievementsConfiguration = achievementsConfiguration;
  }

  public AchievementFactory getAchievementFactory() {
    return this.achievementFactory;
  }

  public AchievementsUserFactory getAchievementsUserFactory() {
    return this.achievementsUserFactory;
  }

  public AchievementsUserRepository getAchievementsUserRepository() {
    return this.achievementsUserRepository;
  }
}
