package pl.rosehc.controller.packet;

import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.auth.AuthConfiguration;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.controller.packet.auth.user.AuthUserCreatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserDeletePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserLastIPUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserLastOnlineUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserMarkRegisteredPacket;
import pl.rosehc.controller.packet.auth.user.AuthUserPasswordUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserSetPremiumStatePacket;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;

public final class AuthPacketHandler implements PacketHandler,
    ConfigurationSynchronizePacketHandler {

  private final AuthPlugin plugin;

  public AuthPacketHandler(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName()
        .equals("pl.rosehc.controller.configuration.impl.configuration.AuthConfiguration")) {
      this.plugin.setAuthConfiguration(
          ConfigurationHelper.deserializeConfiguration(packet.getSerializedConfiguration(),
              AuthConfiguration.class));
    }
  }

  public void handle(final AuthUserCreatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && !this.plugin.getAuthUserFactory()
        .findUser(packet.getNickname()).isPresent()) {
      final AuthUser user = new AuthUser(packet.getNickname(), packet.getLastIP(),
          packet.getFirstJoinTime(), packet.getLastOnlineTime(), packet.isPremium());
      user.setRegistered(packet.isRegistered());
      user.setPassword(packet.getPassword());
      this.plugin.getAuthUserFactory().addUser(user);
    }
  }

  public void handle(final AuthUserDeletePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(this.plugin.getAuthUserFactory()::removeUser);
    }
  }

  public void handle(final AuthUserPasswordUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(user -> user.setPassword(packet.getPassword()));
    }
  }

  public void handle(final AuthUserLastIPUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(user -> user.setLastIP(packet.getLastIp()));
    }
  }

  public void handle(final AuthUserLastOnlineUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(user -> user.setLastOnlineTime(packet.getLastOnlineTime()));
    }
  }

  public void handle(final AuthUserSetPremiumStatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(user -> user.setPremium(packet.getState()));
    }
  }

  public void handle(final AuthUserMarkRegisteredPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getAuthUserFactory().findUser(packet.getNickname())
          .ifPresent(user -> user.setRegistered(true));
    }
  }
}
