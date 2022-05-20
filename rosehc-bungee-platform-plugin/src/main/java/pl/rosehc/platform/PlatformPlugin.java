package pl.rosehc.platform;

import java.util.Arrays;
import java.util.logging.Level;
import me.vaperion.blade.Blade;
import net.md_5.bungee.api.ChatColor;
import pl.rosehc.adapter.command.BungeeCommandContainer;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.adapter.plugin.BungeePlugin;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.PlatformPacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.platform.PlatformInitializationRequestPacket;
import pl.rosehc.controller.packet.platform.PlatformInitializationResponsePacket;
import pl.rosehc.controller.packet.platform.PlatformMotdSettingsSynchronizePacket;
import pl.rosehc.controller.packet.platform.PlatformSetMotdCounterPlayerLimitPacket;
import pl.rosehc.controller.packet.platform.PlatformSetSlotsPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanBroadcastPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanComputerUidUpdatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanCreatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanIpUpdatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBansRequestPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBansResponsePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserComputerUidUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCooldownSynchronizePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCreatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserKickPacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserNicknameUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserRankUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserSendHelpopMessagePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUsersRequestPacket;
import pl.rosehc.controller.packet.platform.user.PlatformUsersResponsePacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistChangeStatePacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistSetReasonPacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistUpdatePlayerPacket;
import pl.rosehc.platform.ban.BanCheckConnectedUsersTask;
import pl.rosehc.platform.ban.BanFactory;
import pl.rosehc.platform.command.PlatformCommandBindings;
import pl.rosehc.platform.command.impl.BanCommand;
import pl.rosehc.platform.command.impl.ConfigReloadCommand;
import pl.rosehc.platform.command.impl.HelpopCommand;
import pl.rosehc.platform.command.impl.KickCommand;
import pl.rosehc.platform.command.impl.ListCommand;
import pl.rosehc.platform.command.impl.SetMaskCommand;
import pl.rosehc.platform.command.impl.SetMotdCommand;
import pl.rosehc.platform.command.impl.SetSlotsCommand;
import pl.rosehc.platform.command.impl.UnBanCommand;
import pl.rosehc.platform.command.impl.WhitelistCommand;
import pl.rosehc.platform.listener.player.PlayerPermissionCheckListener;
import pl.rosehc.platform.listener.player.PlayerPreLoginListener;
import pl.rosehc.platform.listener.server.ProxyPingListener;
import pl.rosehc.platform.listener.server.ServerBlazingPackAuthListener;
import pl.rosehc.platform.listener.server.ServerConnectListener;
import pl.rosehc.platform.listener.server.ServerConnectedListener;
import pl.rosehc.platform.rank.RankFactory;
import pl.rosehc.platform.user.PlatformUserFactory;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.proxy.Proxy;
import pl.rosehc.sectors.proxy.ProxyInitializationHook;

public final class PlatformPlugin extends BungeePlugin implements ProxyInitializationHook {

  private static PlatformPlugin instance;
  private PlatformConfiguration platformConfiguration;
  private RankFactory rankFactory;
  private PlatformUserFactory platformUserFactory;
  private BanFactory banFactory;

  public static PlatformPlugin getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
    SectorsPlugin.getInstance().registerHook(this);
  }

  @Override
  public void onEnable() {
    this.registerListeners(new ProxyPingListener(PlatformPlugin.this));
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Proxy proxy,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new PlatformPacketHandler(this), Arrays.asList(
          "rhc_platform_" + proxy.getIdentifier(),
          "rhc_platform",
          "rhc_global"
      ), Arrays.asList(
          PlatformInitializationResponsePacket.class,
          PlatformUsersResponsePacket.class,
          PlatformUserCreatePacket.class,
          PlatformUserCooldownSynchronizePacket.class,
          PlatformUserNicknameUpdatePacket.class,
          PlatformUserComputerUidUpdatePacket.class,
          PlatformUserRankUpdatePacket.class,
          PlatformUserKickPacket.class,
          PlatformUserSendHelpopMessagePacket.class,
          PlatformBansResponsePacket.class,
          PlatformBanCreatePacket.class,
          PlatformBanDeletePacket.class,
          PlatformBanComputerUidUpdatePacket.class,
          PlatformBanIpUpdatePacket.class,
          PlatformBanBroadcastPacket.class,
          PlatformSetSlotsPacket.class,
          PlatformSetMotdCounterPlayerLimitPacket.class,
          PlatformWhitelistChangeStatePacket.class,
          PlatformWhitelistUpdatePlayerPacket.class,
          PlatformWhitelistSetReasonPacket.class,
          PlatformMotdSettingsSynchronizePacket.class,
          ConfigurationSynchronizePacket.class
      ));
      this.getRedisAdapter().sendPacket(
          new PlatformInitializationRequestPacket(String.valueOf(proxy.getIdentifier())),
          new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              final PlatformInitializationResponsePacket initializationResponsePacket = (PlatformInitializationResponsePacket) packet;
              executeOrSevere(() -> {
                platformConfiguration = ConfigurationHelper.deserializeConfiguration(
                    initializationResponsePacket.getPlatformConfigurationData(),
                    PlatformConfiguration.class);
                rankFactory = new RankFactory(platformConfiguration);
                completionStage.addWaiter(waiter);
                completionStage.removeWaiter(waiter);
                getRedisAdapter().sendPacket(
                    new PlatformUsersRequestPacket(String.valueOf(proxy.getIdentifier())),
                    new Callback() {

                      @Override
                      public void done(final CallbackPacket packet) {
                        final PlatformUsersResponsePacket usersResponsePacket = (PlatformUsersResponsePacket) packet;
                        executeOrSevere(() -> {
                          platformUserFactory = new PlatformUserFactory(
                              usersResponsePacket.getUsers());
                          completionStage.addWaiter(waiter);
                          completionStage.removeWaiter(waiter);
                          getRedisAdapter().sendPacket(
                              new PlatformBansRequestPacket(proxy.getIdentifier()), new Callback() {

                                @Override
                                public void done(final CallbackPacket packet) {
                                  final PlatformBansResponsePacket bansResponsePacket = (PlatformBansResponsePacket) packet;
                                  executeOrSevere(() -> {
                                    banFactory = new BanFactory(bansResponsePacket.getBans());
                                    registerListeners(
                                        new PlayerPermissionCheckListener(PlatformPlugin.this),
                                        new PlayerPreLoginListener(PlatformPlugin.this),
                                        new ServerBlazingPackAuthListener(PlatformPlugin.this),
                                        new ServerConnectListener(PlatformPlugin.this),
                                        new ServerConnectedListener());
                                    final Blade blade = Blade.of()
                                        .executionTimeWarningThreshold(500L)
                                        .defaultPermissionMessage(ChatColor.RED + "Brak uprawnień.")
                                        .containerCreator(BungeeCommandContainer.CREATOR)
                                        .binding(new PlatformCommandBindings(PlatformPlugin.this))
                                        .asyncExecutor(runnable -> getProxy().getScheduler()
                                            .runAsync(PlatformPlugin.this, runnable)).build();
                                    blade.register(new ConfigReloadCommand(PlatformPlugin.this));
                                    blade.register(new WhitelistCommand(PlatformPlugin.this));
                                    blade.register(new SetSlotsCommand(PlatformPlugin.this));
                                    blade.register(new ListCommand(PlatformPlugin.this));
                                    blade.register(new SetMotdCommand(PlatformPlugin.this));
                                    blade.register(new SetMaskCommand(PlatformPlugin.this));
                                    blade.register(new KickCommand(PlatformPlugin.this));
                                    blade.register(new HelpopCommand(PlatformPlugin.this));
                                    blade.register(new BanCommand(PlatformPlugin.this));
                                    blade.register(new UnBanCommand(PlatformPlugin.this));
                                    new BanCheckConnectedUsersTask(PlatformPlugin.this);
                                    completionStage.removeWaiter(waiter);
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
              });
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  public PlatformConfiguration getPlatformConfiguration() {
    return this.platformConfiguration;
  }

  public void setPlatformConfiguration(PlatformConfiguration platformConfiguration) {
    this.platformConfiguration = platformConfiguration;
  }

  public RankFactory getRankFactory() {
    return this.rankFactory;
  }

  public PlatformUserFactory getPlatformUserFactory() {
    return this.platformUserFactory;
  }

  public BanFactory getBanFactory() {
    return this.banFactory;
  }

  private void executeOrSevere(final SafeRunnable action) {
    try {
      action.run();
    } catch (final Exception ex) {
      this.getLogger().log(Level.SEVERE, "Plugin nie mógł zostać poprawnie załadowany!", ex);
    }
  }

  private interface SafeRunnable {

    void run() throws Exception;
  }
}
