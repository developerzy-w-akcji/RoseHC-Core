package pl.rosehc.guilds;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import me.vaperion.blade.Blade;
import me.vaperion.blade.bindings.impl.BukkitBindings;
import me.vaperion.blade.container.impl.BukkitCommandContainer;
import net.md_5.bungee.api.ChatColor;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.plugin.BukkitPlugin;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.GuildPacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.guild.GuildCuboidSchematicSynchronizePacket;
import pl.rosehc.controller.packet.guild.GuildsInitializationRequestPacket;
import pl.rosehc.controller.packet.guild.GuildsInitializationResponsePacket;
import pl.rosehc.controller.packet.guild.guild.GuildAddRegenerationBlocksPacket;
import pl.rosehc.controller.packet.guild.guild.GuildAlertPacket;
import pl.rosehc.controller.packet.guild.guild.GuildAllyInviteEntryUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildCreatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildDeletePacket;
import pl.rosehc.controller.packet.guild.guild.GuildGuildsRequestPacket;
import pl.rosehc.controller.packet.guild.guild.GuildGuildsResponsePacket;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoAddPacket;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoRemovePacket;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildHomeLocationUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildJoinAlertMessageUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberAddPacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberInviteAddPacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberInviteRemovePacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberRemovePacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberUpdateRankPacket;
import pl.rosehc.controller.packet.guild.guild.GuildPistonsUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildPvPUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildRegionUpdateSizePacket;
import pl.rosehc.controller.packet.guild.guild.GuildUpdateAllyPacket;
import pl.rosehc.controller.packet.guild.guild.GuildValidityTimeUpdatePacket;
import pl.rosehc.controller.packet.guild.user.GuildUserCacheFighterPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserCacheVictimPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserClearFightersPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserCreatePacket;
import pl.rosehc.controller.packet.guild.user.GuildUserSynchronizeRankingPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserTeleportOutFromTerrainPacket;
import pl.rosehc.controller.packet.guild.user.GuildUsersRequestPacket;
import pl.rosehc.controller.packet.guild.user.GuildUsersResponsePacket;
import pl.rosehc.controller.wrapper.guild.GuildRegenerationBlockStateSerializationWrapper;
import pl.rosehc.guilds.command.GuildCommandBindings;
import pl.rosehc.guilds.command.user.guild.GuildAlertCommand;
import pl.rosehc.guilds.command.user.guild.GuildAllyCommand;
import pl.rosehc.guilds.command.user.guild.GuildBreakCommand;
import pl.rosehc.guilds.command.user.guild.GuildCreateCommand;
import pl.rosehc.guilds.command.user.guild.GuildDeleteCommand;
import pl.rosehc.guilds.command.user.guild.GuildDeputyCommand;
import pl.rosehc.guilds.command.user.guild.GuildEnlargeCommand;
import pl.rosehc.guilds.command.user.guild.GuildHelpCommand;
import pl.rosehc.guilds.command.user.guild.GuildHomeCommand;
import pl.rosehc.guilds.command.user.guild.GuildInfoCommand;
import pl.rosehc.guilds.command.user.guild.GuildInviteCommand;
import pl.rosehc.guilds.command.user.guild.GuildItemsCommand;
import pl.rosehc.guilds.command.user.guild.GuildJoinCommand;
import pl.rosehc.guilds.command.user.guild.GuildKickCommand;
import pl.rosehc.guilds.command.user.guild.GuildLeaderCommand;
import pl.rosehc.guilds.command.user.guild.GuildLeaveCommand;
import pl.rosehc.guilds.command.user.guild.GuildNeedHelpCommand;
import pl.rosehc.guilds.command.user.guild.GuildPanelCommand;
import pl.rosehc.guilds.command.user.guild.GuildPvpCommand;
import pl.rosehc.guilds.command.user.guild.GuildSetHomeCommand;
import pl.rosehc.guilds.command.user.guild.GuildSetJoinAlertMessageCommand;
import pl.rosehc.guilds.command.user.guild.GuildValidityCommand;
import pl.rosehc.guilds.command.user.user.UserRankingResetCommand;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildFactory;
import pl.rosehc.guilds.guild.GuildRegenerationBlockState;
import pl.rosehc.guilds.guild.group.GuildGroupFactory;
import pl.rosehc.guilds.guild.task.GuildAllyPlayerHelpInfoUpdateTask;
import pl.rosehc.guilds.guild.task.GuildExpiryCheckTask;
import pl.rosehc.guilds.guild.task.GuildGolemUpdateTask;
import pl.rosehc.guilds.guild.task.GuildPistonScanningInformationTask;
import pl.rosehc.guilds.guild.task.GuildPlayerHelpInfoUpdateTask;
import pl.rosehc.guilds.guild.task.GuildRegionEnterLeaveTask;
import pl.rosehc.guilds.guild.task.GuildTerrainBossBarUpdateTask;
import pl.rosehc.guilds.guild.task.GuildUpdateRegenerationBlocksTask;
import pl.rosehc.guilds.listener.block.AntiGriefBlockPlaceListener;
import pl.rosehc.guilds.listener.block.BlockBreakListener;
import pl.rosehc.guilds.listener.block.BlockCenterProtectionListener;
import pl.rosehc.guilds.listener.block.BlockFromToListener;
import pl.rosehc.guilds.listener.block.BlockPlaceListener;
import pl.rosehc.guilds.listener.entity.EntityDamageByEntityListener;
import pl.rosehc.guilds.listener.entity.EntityExplodeListener;
import pl.rosehc.guilds.listener.player.AsyncPlayerChatListener;
import pl.rosehc.guilds.listener.player.AsyncPlayerPreLoginListener;
import pl.rosehc.guilds.listener.player.PlayerBucketEmptyListener;
import pl.rosehc.guilds.listener.player.PlayerBucketFillListener;
import pl.rosehc.guilds.listener.player.PlayerCommandPreprocessListener;
import pl.rosehc.guilds.listener.player.PlayerDeathListener;
import pl.rosehc.guilds.listener.player.PlayerInteractListener;
import pl.rosehc.guilds.listener.player.PlayerJoinListener;
import pl.rosehc.guilds.listener.player.PlayerNameTagListener;
import pl.rosehc.guilds.listener.player.PlayerQuitListener;
import pl.rosehc.guilds.listener.sector.SectorUserJoinListener;
import pl.rosehc.guilds.ranking.RankingFactory;
import pl.rosehc.guilds.ranking.recalculation.RankingRecalculationTask;
import pl.rosehc.guilds.schematic.SchematicFactory;
import pl.rosehc.guilds.tablist.TabListFactory;
import pl.rosehc.guilds.tablist.TabListPlaceholders;
import pl.rosehc.guilds.tablist.TabListUpdateTask;
import pl.rosehc.guilds.user.GuildUserFactory;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorInitializationHook;

public final class GuildsPlugin extends BukkitPlugin implements SectorInitializationHook {

  private static GuildsPlugin instance;
  private GuildsConfiguration guildsConfiguration;
  private GuildGroupFactory guildGroupFactory;
  private GuildUserFactory guildUserFactory;
  private GuildFactory guildFactory;
  private SchematicFactory schematicFactory;
  private RankingFactory rankingFactory;
  private TabListFactory tabListFactory;

  public static GuildsPlugin getInstance() {
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
      this.getRedisAdapter().subscribe(new GuildPacketHandler(this), Arrays.asList(
          "rhc_global",
          "rhc_guilds",
          "rhc_guilds_" + sector.getName()
      ), Arrays.asList(
          ConfigurationSynchronizePacket.class,
          GuildsInitializationResponsePacket.class,
          GuildCuboidSchematicSynchronizePacket.class,
          GuildUserCacheFighterPacket.class,
          GuildUserCacheVictimPacket.class,
          GuildUserClearFightersPacket.class,
          GuildUserCreatePacket.class,
          GuildUsersResponsePacket.class,
          GuildUserSynchronizeRankingPacket.class,
          GuildUserTeleportOutFromTerrainPacket.class,
          GuildAddRegenerationBlocksPacket.class,
          GuildAlertPacket.class,
          GuildAllyInviteEntryUpdatePacket.class,
          GuildCreatePacket.class,
          GuildDeletePacket.class,
          GuildHelpInfoAddPacket.class,
          GuildHelpInfoRemovePacket.class,
          GuildHelpInfoUpdatePacket.class,
          GuildHomeLocationUpdatePacket.class,
          GuildJoinAlertMessageUpdatePacket.class,
          GuildMemberAddPacket.class,
          GuildMemberInviteAddPacket.class,
          GuildMemberInviteRemovePacket.class,
          GuildMemberRemovePacket.class,
          GuildPistonsUpdatePacket.class,
          GuildPvPUpdatePacket.class,
          GuildRegionUpdateSizePacket.class,
          GuildUpdateAllyPacket.class,
          GuildValidityTimeUpdatePacket.class,
          GuildMemberUpdateRankPacket.class,
          GuildGuildsResponsePacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new GuildsInitializationRequestPacket(sector.getName()), new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              executeOrDisable(() -> {
                final GuildsInitializationResponsePacket initializationResponsePacket = (GuildsInitializationResponsePacket) packet;
                guildsConfiguration = ConfigurationHelper.deserializeConfiguration(
                    initializationResponsePacket.getConfigurationData(), GuildsConfiguration.class);
                guildGroupFactory = new GuildGroupFactory(guildsConfiguration);
                schematicFactory = new SchematicFactory();
                guildsConfiguration.pluginWrapper.parsedVictimKillerConsiderationTimeoutTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.victimKillerConsiderationTimeoutTime);
                guildsConfiguration.pluginWrapper.parsedVictimKillerLastKillTimeoutTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.victimKillerLastKillTimeoutTime);
                guildsConfiguration.pluginWrapper.parsedStartGuildProtectionTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.startGuildProtectionTime);
                guildsConfiguration.pluginWrapper.parsedStartGuildValidityTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.startGuildValidityTime);
                guildsConfiguration.pluginWrapper.parsedAddGuildValidityTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.addGuildValidityTime);
                guildsConfiguration.pluginWrapper.parsedWhenGuildValidityTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.whenGuildValidityTime);
                guildsConfiguration.pluginWrapper.parsedMaxGuildValidityTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.maxGuildValidityTime);
                guildsConfiguration.pluginWrapper.parsedMinGuildValidityTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.minGuildValidityTime);
                guildsConfiguration.pluginWrapper.parsedGuildMemberInviteConsiderationTimeoutTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.guildMemberInviteConsiderationTimeoutTime);
                guildsConfiguration.pluginWrapper.parsedGuildAllyInviteConsiderationTimeoutTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.guildAllyInviteConsiderationTimeoutTime);
                guildsConfiguration.pluginWrapper.parsedTntExplosionTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.tntExplosionTime);
                guildsConfiguration.pluginWrapper.parsedTntExplosionNotificationTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.tntExplosionNotificationTime);
                guildsConfiguration.pluginWrapper.parsedGuildNeedHelpWaypointTime = TimeHelper.timeFromString(
                    guildsConfiguration.pluginWrapper.guildNeedHelpWaypointTime);
                schematicFactory.reloadGuildSchematic(
                    initializationResponsePacket.getGuildSchematicData());
                completionStage.addWaiter(waiter);
                completionStage.removeWaiter(waiter);
                getRedisAdapter().sendPacket(new GuildUsersRequestPacket(sector.getName()),
                    new Callback() {

                      @Override
                      public void done(final CallbackPacket packet) {
                        executeOrDisable(() -> {
                          final GuildUsersResponsePacket usersResponsePacket = (GuildUsersResponsePacket) packet;
                          guildUserFactory = new GuildUserFactory(usersResponsePacket.getUsers());
                          completionStage.addWaiter(waiter);
                          completionStage.removeWaiter(waiter);
                          getRedisAdapter().sendPacket(
                              new GuildGuildsRequestPacket(sector.getName()), new Callback() {

                                @Override
                                public void done(final CallbackPacket packet) {
                                  executeOrDisable(() -> {
                                    final GuildGuildsResponsePacket guildsResponsePacket = (GuildGuildsResponsePacket) packet;
                                    guildFactory = new GuildFactory(
                                        guildsResponsePacket.getGuildSerializableWrapperList());
                                    rankingFactory = new RankingFactory();
                                    tabListFactory = new TabListFactory();
                                    tabListFactory.updateTabListElements(guildsConfiguration);
                                    TabListPlaceholders.registerDefaults();
                                    registerListeners(
                                        new AsyncPlayerPreLoginListener(GuildsPlugin.this),
                                        new AsyncPlayerChatListener(GuildsPlugin.this),
                                        new PlayerBucketEmptyListener(GuildsPlugin.this),
                                        new PlayerBucketFillListener(GuildsPlugin.this),
                                        new PlayerCommandPreprocessListener(GuildsPlugin.this),
                                        new PlayerDeathListener(GuildsPlugin.this),
                                        new PlayerInteractListener(GuildsPlugin.this),
                                        new PlayerJoinListener(GuildsPlugin.this),
                                        new PlayerQuitListener(GuildsPlugin.this),
                                        new SectorUserJoinListener(GuildsPlugin.this),
                                        new EntityDamageByEntityListener(GuildsPlugin.this),
                                        new EntityExplodeListener(GuildsPlugin.this),
                                        new AntiGriefBlockPlaceListener(GuildsPlugin.this),
                                        new BlockBreakListener(GuildsPlugin.this),
                                        new BlockCenterProtectionListener(GuildsPlugin.this),
                                        new BlockFromToListener(GuildsPlugin.this),
                                        new BlockPlaceListener(GuildsPlugin.this),
                                        new PlayerNameTagListener(GuildsPlugin.this));
                                  });
                                  final Blade blade = Blade.of().fallbackPrefix("guilds")
                                      .defaultPermissionMessage(ChatColor.RED + "Brak uprawnień.")
                                      .overrideCommands(true).executionTimeWarningThreshold(1500L)
                                      .containerCreator(BukkitCommandContainer.CREATOR)
                                      .binding(new GuildCommandBindings(GuildsPlugin.this))
                                      .binding(new BukkitBindings()).asyncExecutor(
                                          runnable -> getServer().getScheduler()
                                              .runTaskAsynchronously(GuildsPlugin.this, runnable))
                                      .build();

                                  blade.register(new GuildHelpCommand(GuildsPlugin.this));
                                  blade.register(new GuildCreateCommand(GuildsPlugin.this));
                                  blade.register(new GuildDeleteCommand(GuildsPlugin.this));
                                  blade.register(new GuildInviteCommand(GuildsPlugin.this));
                                  blade.register(new GuildKickCommand(GuildsPlugin.this));
                                  blade.register(new GuildJoinCommand(GuildsPlugin.this));
                                  blade.register(new GuildLeaveCommand(GuildsPlugin.this));
                                  blade.register(new GuildEnlargeCommand(GuildsPlugin.this));
                                  blade.register(new GuildAlertCommand(GuildsPlugin.this));
                                  blade.register(new GuildAllyCommand(GuildsPlugin.this));
                                  blade.register(new GuildBreakCommand(GuildsPlugin.this));
                                  blade.register(new GuildLeaderCommand(GuildsPlugin.this));
                                  blade.register(new GuildDeputyCommand(GuildsPlugin.this));
                                  blade.register(new GuildHomeCommand(GuildsPlugin.this));
                                  blade.register(new GuildSetHomeCommand(GuildsPlugin.this));
                                  blade.register(new GuildInfoCommand(GuildsPlugin.this));
                                  blade.register(new GuildNeedHelpCommand(GuildsPlugin.this));
                                  blade.register(new GuildValidityCommand(GuildsPlugin.this));
                                  blade.register(
                                      new GuildSetJoinAlertMessageCommand(GuildsPlugin.this));
                                  blade.register(new GuildPanelCommand(GuildsPlugin.this));
                                  blade.register(new GuildPvpCommand(GuildsPlugin.this));
                                  blade.register(new GuildItemsCommand());
                                  blade.register(new UserRankingResetCommand(GuildsPlugin.this));

                                  new GuildAllyPlayerHelpInfoUpdateTask(GuildsPlugin.this);
                                  new GuildPlayerHelpInfoUpdateTask(GuildsPlugin.this);
                                  new GuildExpiryCheckTask(GuildsPlugin.this);
                                  new GuildGolemUpdateTask(GuildsPlugin.this);
                                  new GuildPistonScanningInformationTask(GuildsPlugin.this);
                                  new GuildRegionEnterLeaveTask(GuildsPlugin.this);
                                  new GuildTerrainBossBarUpdateTask(GuildsPlugin.this);
                                  new GuildUpdateRegenerationBlocksTask(GuildsPlugin.this);
                                  new RankingRecalculationTask(GuildsPlugin.this);
                                  new TabListUpdateTask(GuildsPlugin.this);
                                  completionStage.removeWaiter(waiter);
                                }

                                @Override
                                public void error(final String ignored) {
                                }
                              }, "rhc_master_controller");
                        });
                      }

                      @Override
                      public void error(final String ignored) {
                      }
                    }, "rhc_master_controller");
              });
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  @Override
  public void onDisable() {
    if (this.guildFactory != null) {
      for (final Guild guild : this.guildFactory.getGuildMap().values()) {
        final List<GuildRegenerationBlockStateSerializationWrapper> blockStateList = guild.getNewlyAddedRegenerationBlocks()
            .stream().map(GuildRegenerationBlockState::wrap).collect(Collectors.toList());
        if (!blockStateList.isEmpty()) {
          this.getRedisAdapter()
              .sendPacket(new GuildAddRegenerationBlocksPacket(guild.getTag(), blockStateList),
                  "rhc_master_controller");
        }
      }
    }
  }

  public GuildsConfiguration getGuildsConfiguration() {
    return this.guildsConfiguration;
  }

  public void setGuildsConfiguration(final GuildsConfiguration guildsConfiguration) {
    this.guildsConfiguration = guildsConfiguration;
  }

  public GuildUserFactory getGuildUserFactory() {
    return this.guildUserFactory;
  }

  public GuildGroupFactory getGuildGroupFactory() {
    return this.guildGroupFactory;
  }

  public GuildFactory getGuildFactory() {
    return this.guildFactory;
  }

  public SchematicFactory getSchematicFactory() {
    return this.schematicFactory;
  }

  public RankingFactory getRankingFactory() {
    return this.rankingFactory;
  }

  public TabListFactory getTabListFactory() {
    return this.tabListFactory;
  }

  private void executeOrDisable(final SafeRunnable action) {
    try {
      action.run();
    } catch (final Throwable ex) {
      this.getLogger().log(Level.SEVERE, "Plugin nie mógł zostać poprawnie załadowany!", ex);
      this.getServer().getPluginManager().disablePlugin(this);
    }
  }

  private interface SafeRunnable {

    void run() throws Exception;
  }
}
