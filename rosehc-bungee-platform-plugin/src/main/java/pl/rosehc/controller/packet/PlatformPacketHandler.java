package pl.rosehc.controller.packet;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.platform.PlatformMotdSettingsSynchronizePacket;
import pl.rosehc.controller.packet.platform.PlatformSetMotdCounterPlayerLimitPacket;
import pl.rosehc.controller.packet.platform.PlatformSetSlotsPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanBroadcastPacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanComputerUidUpdatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanCreatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanDeletePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanIpUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserComputerUidUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCooldownSynchronizePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCreatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserKickPacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserNicknameUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserRankUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserSendHelpopMessagePacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistChangeStatePacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistSetReasonPacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistUpdatePlayerPacket;
import pl.rosehc.platform.PlatformConfiguration;
import pl.rosehc.platform.PlatformConfiguration.ProxyMotdWrapper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.ban.Ban;
import pl.rosehc.platform.rank.Rank;
import pl.rosehc.platform.rank.RankEntry;
import pl.rosehc.platform.rank.RankFactory;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.sectors.SectorsPlugin;

public final class PlatformPacketHandler implements PacketHandler,
    ConfigurationSynchronizePacketHandler {

  private final PlatformPlugin plugin;

  public PlatformPacketHandler(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName()
        .equals("pl.rosehc.controller.configuration.impl.configuration.PlatformConfiguration")) {
      final PlatformConfiguration configuration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), PlatformConfiguration.class);
      final RankFactory rankFactory = this.plugin.getRankFactory();
      this.plugin.setPlatformConfiguration(configuration);
      if (Objects.nonNull(rankFactory) && Objects.nonNull(this.plugin.getPlatformUserFactory())) {
        rankFactory.getRankMap().clear();
        rankFactory.getRankMap().putAll(configuration.rankList.stream().map(
            rankWrapper -> new Rank(rankWrapper.name, rankWrapper.chatPrefix,
                rankWrapper.chatSuffix, rankWrapper.nameTagPrefix, rankWrapper.nameTagSuffix,
                rankWrapper.permissions, rankWrapper.priority, rankWrapper.defaultRank)).collect(
            Collectors.toConcurrentMap(rank -> rank.getName().toLowerCase(), rank -> rank)));
        rankFactory.updateDefaultRank();
        for (final PlatformUser user : this.plugin.getPlatformUserFactory().getUserMap().values()) {
          final RankEntry rank = user.getRank();
          final boolean previousRankNotFound =
              rank.getPreviousRank() != null && !rankFactory.getRankMap()
                  .containsKey(rank.getPreviousRank().getName().toLowerCase());
          if (previousRankNotFound || !rankFactory.getRankMap()
              .containsKey(rank.getCurrentRank().getNameTagPrefix().toLowerCase())) {
            final RankEntry newRank = RankEntry.create(
                previousRankNotFound ? rankFactory.getDefaultRank() : rank.getPreviousRank(),
                !rankFactory.getRankMap().containsKey(rank.getCurrentRank().getName().toLowerCase())
                    ? rankFactory.getDefaultRank() : rank.getCurrentRank(),
                !previousRankNotFound ? rank.getExpirationTime() : 0L);
            user.setRank(newRank);
          }
        }
      }
    }
  }

  public void handle(final PlatformUserSendHelpopMessagePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      //noinspection SpellCheckingInspection
      final String formattedHelpopMessage = ChatHelper.colored(
              this.plugin.getPlatformConfiguration().messagesWrapper.helpopFormat.replace(
                      "{PROXY_IDENTIFIER}", String.format("%02d", packet.getProxyIdentifier()))
                  .replace("{SECTOR_NAME}", packet.getSectorName())
                  .replace("{PLAYER_NAME}", packet.getPlayerName()))
          .replace("{MESSAGE}", packet.getMessage());
      for (final ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
        if (player.hasPermission("platform-helpop-see")) {
          player.sendMessage(formattedHelpopMessage);
        }
      }
    }
  }

  public void handle(final PlatformUserRankUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(packet.getUniqueId()).ifPresent(
          user -> user.setRank(RankEntry.create(
              packet.getPreviousRankName() != null ? this.plugin.getRankFactory()
                  .findRank(packet.getPreviousRankName()).orElse(null) : null,
              this.plugin.getRankFactory().findRank(packet.getCurrentRankName())
                  .orElse(this.plugin.getRankFactory().getDefaultRank()),
              packet.getExpirationTime())));
    }
  }

  public void handle(final PlatformBanBroadcastPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      for (final ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
        if (!packet.isSilent() || player.hasPermission("platform-ban-see")) {
          ChatHelper.sendMessage(player, packet.getBroadcastMessage());
        }
      }
    }
  }

  public void handle(final PlatformMotdSettingsSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      final ProxyMotdWrapper proxyMotdWrapper = this.plugin.getPlatformConfiguration().proxyMotdWrapper;
      proxyMotdWrapper.firstLine = packet.getFirstLine();
      proxyMotdWrapper.secondLine = packet.getSecondLine();
      proxyMotdWrapper.thirdLine = packet.getThirdLine();
      proxyMotdWrapper.thirdLineSpacing = packet.getThirdLineSpacing();
    }
  }

  public void handle(final PlatformWhitelistUpdatePlayerPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.remove(
          packet.getPlayerName());
      if (packet.isAdd()) {
        this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.add(
            packet.getPlayerName());
      }
    }
  }

  public void handle(final PlatformBanCreatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && !this.plugin.getBanFactory()
        .findBan(packet.getPlayerNickname()).isPresent()) {
      final Ban ban = new Ban(packet.getPlayerNickname(), packet.getStaffNickname(), packet.getIp(),
          packet.getReason(), packet.getComputerUid(), packet.getCreationTime(),
          packet.getLeftTime());
      this.plugin.getBanFactory().addBan(ban);
    }
  }

  public void handle(final PlatformSetSlotsPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      if (packet.isProxy()) {
        this.plugin.getPlatformConfiguration().slotWrapper.proxySlots = packet.getSlots();
      } else {
        this.plugin.getPlatformConfiguration().slotWrapper.spigotSlots = packet.getSlots();
      }
    }
  }

  public void handle(final PlatformUserCreatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && !this.plugin.getPlatformUserFactory()
        .findUserByUniqueId(packet.getUniqueId()).isPresent()) {
      final PlatformUser user = new PlatformUser(packet.getUniqueId(), packet.getNickname());
      this.plugin.getPlatformUserFactory().addUser(user);
    }
  }

  public void handle(final PlatformUserKickPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      Optional.ofNullable(this.plugin.getProxy().getPlayer(packet.getUniqueId()))
          .ifPresent(player -> player.disconnect(ChatHelper.colored(packet.getKickMessage())));
    }
  }

  public void handle(final PlatformUserCooldownSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(packet.getUniqueId()).ifPresent(
          user -> user.getCooldownCache().putUserCooldown(packet.getType(), packet.getTime()));
    }
  }

  public void handle(final PlatformUserComputerUidUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(packet.getUniqueId())
          .ifPresent(user -> user.setComputerUid(packet.getComputerUid()));
    }
  }

  public void handle(final PlatformUserNicknameUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformUserFactory().findUserByUniqueId(packet.getUniqueId())
          .ifPresent(user -> user.setNickname(packet.getNickname()));
    }
  }

  public void handle(final PlatformBanComputerUidUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getBanFactory().findBan(packet.getPlayerNickname())
          .ifPresent(ban -> ban.setComputerUid(packet.getComputerUid()));
    }
  }

  public void handle(final PlatformBanIpUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getBanFactory().findBan(packet.getPlayerNickname())
          .ifPresent(ban -> ban.setIp(packet.getIp()));
    }
  }

  public void handle(final PlatformBanDeletePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getBanFactory().findBan(packet.getPlayerNickname())
          .ifPresent(this.plugin.getBanFactory()::removeBan);
    }
  }

  public void handle(final PlatformSetMotdCounterPlayerLimitPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformConfiguration().proxyMotdWrapper.counterPlayersLimit = packet.getLimit();
    }
  }

  public void handle(final PlatformWhitelistChangeStatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.enabled = packet.getState();
    }
  }

  public void handle(final PlatformWhitelistSetReasonPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.reason = packet.getReason();
    }
  }
}
