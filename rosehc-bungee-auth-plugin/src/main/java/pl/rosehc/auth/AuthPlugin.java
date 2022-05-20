package pl.rosehc.auth;

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
import pl.rosehc.auth.command.AuthCommand;
import pl.rosehc.auth.command.AuthCommandBindings;
import pl.rosehc.auth.command.ChangePasswordCommand;
import pl.rosehc.auth.command.LoginCommand;
import pl.rosehc.auth.command.RegisterCommand;
import pl.rosehc.auth.listener.login.PostLoginListener;
import pl.rosehc.auth.listener.login.PreLoginListener;
import pl.rosehc.auth.listener.player.BanCreateListener;
import pl.rosehc.auth.listener.player.ChatListener;
import pl.rosehc.auth.listener.player.PlayerDisconnectListener;
import pl.rosehc.auth.listener.server.ServerBlazingPackAuthListener;
import pl.rosehc.auth.listener.server.ServerConnectListener;
import pl.rosehc.auth.listener.server.ServerConnectedListener;
import pl.rosehc.auth.listener.server.ServerKickListener;
import pl.rosehc.auth.mojang.MojangRequestTask;
import pl.rosehc.auth.user.AuthUserCheckCachedProfileTask;
import pl.rosehc.auth.user.AuthUserFactory;
import pl.rosehc.auth.user.AuthUserTimeoutTask;
import pl.rosehc.controller.packet.AuthPacketHandler;
import pl.rosehc.controller.packet.auth.AuthInitializationRequestPacket;
import pl.rosehc.controller.packet.auth.AuthInitializationResponsePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserCreatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserDeletePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserLastIPUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserLastOnlineUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserMarkRegisteredPacket;
import pl.rosehc.controller.packet.auth.user.AuthUserPasswordUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserSetPremiumStatePacket;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.proxy.Proxy;
import pl.rosehc.sectors.proxy.ProxyInitializationHook;

public final class AuthPlugin extends BungeePlugin implements ProxyInitializationHook {

  private static AuthPlugin instance;
  private AuthConfiguration authConfiguration;
  private AuthUserFactory authUserFactory;

  public static AuthPlugin getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
    SectorsPlugin.getInstance().registerHook(this);
  }

  @Override
  public void onInitialize(final EventCompletionStage completionStage, final Proxy proxy,
      final boolean success) {
    if (success) {
      final Object waiter = new Object();
      completionStage.addWaiter(waiter);
      this.getRedisAdapter().subscribe(new AuthPacketHandler(this), Arrays.asList(
          "rhc_auth_" + proxy.getIdentifier(),
          "rhc_auth",
          "rhc_global"
      ), Arrays.asList(
          AuthInitializationResponsePacket.class,
          AuthUserCreatePacket.class,
          AuthUserDeletePacket.class,
          AuthUserLastIPUpdatePacket.class,
          AuthUserLastOnlineUpdatePacket.class,
          AuthUserMarkRegisteredPacket.class,
          AuthUserPasswordUpdatePacket.class,
          AuthUserSetPremiumStatePacket.class,
          ConfigurationSynchronizePacket.class
      ));
      this.getRedisAdapter()
          .sendPacket(new AuthInitializationRequestPacket(proxy.getIdentifier()), new Callback() {

            @Override
            public void done(final CallbackPacket packet) {
              final AuthInitializationResponsePacket responsePacket = (AuthInitializationResponsePacket) packet;
              try {
                authConfiguration = ConfigurationHelper.deserializeConfiguration(
                    responsePacket.getSerializedConfigurationData(), AuthConfiguration.class);
                authUserFactory = new AuthUserFactory(responsePacket.getUsers());
                registerListeners(new PreLoginListener(AuthPlugin.this),
                    new PostLoginListener(AuthPlugin.this),
                    new BanCreateListener(AuthPlugin.this),
                    new PlayerDisconnectListener(AuthPlugin.this),
                    new ChatListener(AuthPlugin.this),
                    new ServerBlazingPackAuthListener(AuthPlugin.this),
                    new ServerConnectListener(AuthPlugin.this),
                    new ServerConnectedListener(AuthPlugin.this),
                    new ServerKickListener(AuthPlugin.this));
                final Blade blade = Blade.of().executionTimeWarningThreshold(1500L)
                    .defaultPermissionMessage(ChatColor.RED + "Brak uprawnień.")
                    .containerCreator(BungeeCommandContainer.CREATOR)
                    .binding(new AuthCommandBindings(AuthPlugin.this)).asyncExecutor(
                        runnable -> getProxy().getScheduler().runAsync(AuthPlugin.this, runnable))
                    .build();
                blade.register(new AuthCommand(AuthPlugin.this));
                blade.register(new RegisterCommand(AuthPlugin.this));
                blade.register(new LoginCommand(AuthPlugin.this));
                blade.register(new ChangePasswordCommand(AuthPlugin.this));
                new AuthUserTimeoutTask(AuthPlugin.this);
                new AuthUserCheckCachedProfileTask(AuthPlugin.this);
                new MojangRequestTask(AuthPlugin.this);
              } catch (final Throwable ex) {
                getLogger().log(Level.SEVERE, "Plugin nie mógł zostać poprawnie załadowany!", ex);
              }
            }

            @Override
            public void error(final String ignored) {
            }
          }, "rhc_master_controller");
    }
  }

  public AuthConfiguration getAuthConfiguration() {
    return this.authConfiguration;
  }

  public void setAuthConfiguration(final AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  public AuthUserFactory getAuthUserFactory() {
    return this.authUserFactory;
  }
}
