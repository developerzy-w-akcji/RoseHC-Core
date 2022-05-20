package pl.rosehc.controller.packet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.controller.packet.guild.GuildCuboidSchematicSynchronizePacket;
import pl.rosehc.controller.packet.guild.guild.GuildAlertPacket;
import pl.rosehc.controller.packet.guild.guild.GuildAllyInviteEntryUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildCreatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildDeletePacket;
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
import pl.rosehc.controller.wrapper.guild.GuildMemberSerializableWrapper;
import pl.rosehc.controller.wrapper.guild.GuildPermissionTypeWrapper;
import pl.rosehc.guilds.GuildsConfiguration;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildHelper;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPlayerHelpInfo;
import pl.rosehc.guilds.guild.GuildRegion;
import pl.rosehc.guilds.guild.group.GuildGroup;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.sectors.SectorsPlugin;

public final class GuildPacketHandler implements PacketHandler,
    ConfigurationSynchronizePacketHandler {

  private final GuildsPlugin plugin;

  public GuildPacketHandler(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName()
        .equals("pl.rosehc.controller.configuration.impl.configuration.GuildsConfiguration")) {
      final GuildsConfiguration configuration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), GuildsConfiguration.class);
      configuration.pluginWrapper.parsedVictimKillerConsiderationTimeoutTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.victimKillerConsiderationTimeoutTime);
      configuration.pluginWrapper.parsedVictimKillerLastKillTimeoutTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.victimKillerLastKillTimeoutTime);
      configuration.pluginWrapper.parsedStartGuildProtectionTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.startGuildProtectionTime);
      configuration.pluginWrapper.parsedStartGuildValidityTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.startGuildValidityTime);
      configuration.pluginWrapper.parsedAddGuildValidityTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.addGuildValidityTime);
      configuration.pluginWrapper.parsedWhenGuildValidityTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.whenGuildValidityTime);
      configuration.pluginWrapper.parsedMaxGuildValidityTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.maxGuildValidityTime);
      configuration.pluginWrapper.parsedMinGuildValidityTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.minGuildValidityTime);
      configuration.pluginWrapper.parsedGuildMemberInviteConsiderationTimeoutTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.guildMemberInviteConsiderationTimeoutTime);
      configuration.pluginWrapper.parsedGuildAllyInviteConsiderationTimeoutTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.guildAllyInviteConsiderationTimeoutTime);
      configuration.pluginWrapper.parsedTntExplosionTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.tntExplosionTime);
      configuration.pluginWrapper.parsedTntExplosionNotificationTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.tntExplosionNotificationTime);
      configuration.pluginWrapper.parsedGuildNeedHelpWaypointTime = TimeHelper.timeFromString(
          configuration.pluginWrapper.guildNeedHelpWaypointTime);
      this.plugin.getTabListFactory().updateTabListElements(configuration);
      this.plugin.getGuildGroupFactory().getDefaultGuildGroupMap().clear();
      this.plugin.getGuildGroupFactory().getDefaultGuildGroupMap().putAll(
          configuration.pluginWrapper.groupMap.entrySet().stream().map(
                  entry -> new GuildGroup(entry.getKey(),
                      entry.getValue().permissions.stream().map(GuildPermissionTypeWrapper::toOriginal)
                          .collect(Collectors.toSet()), entry.getValue().color.toOriginal(),
                      entry.getValue().leader, entry.getValue().deputy, entry.getValue().name))
              .collect(Collectors.toConcurrentMap(GuildGroup::getUniqueId, group -> group)));
      this.plugin.getGuildGroupFactory().updateDefaultGroups(configuration);
      this.plugin.setGuildsConfiguration(configuration);
    }
  }

  public void handle(final GuildCreatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && !this.plugin.getGuildFactory()
        .findGuildByCredential(packet.getTag()).isPresent() && !this.plugin.getGuildFactory()
        .findGuildByCredential(packet.getName(), false).isPresent()) {
      final Map<UUID, GuildGroup> defaultGuildGroupMap = GuildHelper.createDefaultGuildGroups();
      final Guild guild = new Guild(packet.getName(), packet.getTag(), defaultGuildGroupMap,
          new GuildMember(packet.getLeader().getUniqueId(),
              this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getLeader().getUniqueId())
                  .orElseThrow(() -> new UnsupportedOperationException(
                      "Brak użytkownika o identyfikatorze " + packet.getLeader().getUniqueId()
                          + "!")), packet.getLeader().getPermissions().stream()
              .map(GuildPermissionTypeWrapper::toOriginal).collect(Collectors.toSet()),
              defaultGuildGroupMap.get(
                  this.plugin.getGuildGroupFactory().getLeaderGuildGroup().getUniqueId())),
          packet.getGuildType().toOriginal(), new GuildRegion(Objects.requireNonNull(
          SerializeHelper.deserializeLocation(packet.getGuildRegion().getCenterLocation())),
          packet.getGuildRegion().getSize()),
          SectorsPlugin.getInstance().getSectorFactory().findSector(packet.getCreationSectorName())
              .orElseThrow(() -> new UnsupportedOperationException(
                  "Brak sektora o nazwie " + packet.getCreationSectorName() + "!")),
          SerializeHelper.deserializeLocation(packet.getHomeLocation()), packet.getValidityTime(),
          packet.getProtectionTime(), packet.getLives());
      this.plugin.getGuildFactory().registerGuild(guild);
    }
  }

  public void handle(final GuildAlertPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> {
            for (final GuildMember member : guild.getGuildMembers()) {
              if (member != null) {
                final Player player = this.plugin.getServer().getPlayer(member.getUniqueId());
                if (player == null) {
                  continue;
                }

                ChatHelper.sendTitle(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.guildAlertTitle,
                    packet.getMessage(), 0, 60, 20);
              }
            }
          });
    }
  }

  public void handle(final GuildHelpInfoAddPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> {
            final GuildPlayerHelpInfo helpInfo = new GuildPlayerHelpInfo(packet.getNickname(),
                packet.getTime(), packet.getX(), packet.getY(), packet.getZ());
            if (packet.isAlly()) {
              guild.addGuildAllyPlayerHelpInfo(packet.getUniqueId(), helpInfo);
            } else {
              guild.addGuildPlayerHelpInfo(packet.getUniqueId(), helpInfo);
            }

            GuildHelper.updateGuildHelpWaypoint(helpInfo, guild, packet.isAlly());
          });
    }
  }

  public void handle(final GuildHelpInfoRemovePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      final boolean ally = packet.isAlly();
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true).ifPresent(
          guild -> (ally ? guild.findGuildAllyPlayerHelpInfo(packet.getUniqueId())
              : guild.findGuildPlayerHelpInfo(packet.getUniqueId())).ifPresent(helpInfo -> {
            if (ally) {
              guild.removeGuildAllyPlayerHelpInfo(packet.getUniqueId());
            } else {
              guild.removeGuildPlayerHelpInfo(packet.getUniqueId());
            }

            GuildHelper.removeGuildHelpWaypoint(guild, helpInfo, ally);
          }));
    }
  }

  public void handle(final GuildMemberAddPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> {
            final GuildMemberSerializableWrapper guildMemberWrapper = packet.getGuildMemberWrapper();
            guild.addGuildMember(new GuildMember(guildMemberWrapper.getUniqueId(),
                this.plugin.getGuildUserFactory()
                    .findUserByUniqueId(guildMemberWrapper.getUniqueId()).orElseThrow(
                        () -> new UnsupportedOperationException(
                            "Brak użytkownika o identyfikatorze " + guildMemberWrapper.getUniqueId()
                                + "!")), new HashSet<>(), guild.getDefaultGroup()));
          });
    }
  }

  public void handle(final GuildHelpInfoUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true).ifPresent(
          guild -> (packet.isAlly() ? guild.findGuildAllyPlayerHelpInfo(packet.getUniqueId())
              : guild.findGuildPlayerHelpInfo(packet.getUniqueId())).ifPresent(helpInfo -> {
            helpInfo.setX(packet.getX());
            helpInfo.setY(packet.getY());
            helpInfo.setZ(packet.getZ());
            GuildHelper.updateGuildHelpWaypoint(helpInfo, guild, packet.isAlly());
          }));
    }
  }

  public void handle(final GuildUserSynchronizeRankingPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getUniqueId())
          .map(GuildUser::getUserRanking).ifPresent(guildUserRanking -> {
            guildUserRanking.setPoints(packet.getPoints());
            guildUserRanking.setKills(packet.getKills());
            guildUserRanking.setDeaths(packet.getDeaths());
            guildUserRanking.setKillStreak(packet.getKillStreak());
          });
    }
  }

  public void handle(final GuildPvPUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> {
            if (packet.isAlly()) {
              guild.setPvpAlly(packet.getStatus());
            } else {
              guild.setPvpGuild(packet.getStatus());
            }
          });
    }
  }

  public void handle(final GuildUpdateAllyPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getFirstGuildTag(), true)
          .ifPresent(firstGuild -> this.plugin.getGuildFactory()
              .findGuildByCredential(packet.getSecondGuildTag(), true).ifPresent(secondGuild -> {
                firstGuild.setAlliedGuild(packet.isAdd() ? secondGuild : null);
                secondGuild.setAlliedGuild(packet.isAdd() ? firstGuild : null);
              }));
    }
  }

  public void handle(final GuildDeletePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getTag()).ifPresent(guild -> {
        this.plugin.getGuildFactory().unregisterGuild(guild);
        for (final GuildMember guildMember : guild.getGuildMembers()) {
          if (guildMember != null) {
            guildMember.getUser().setMemberArrayPosition(-1);
            guildMember.getUser().setGuild(null);
          }
        }

        final Guild alliedGuild = guild.getAlliedGuild();
        if (alliedGuild != null) {
          alliedGuild.setAlliedGuild(null);
        }
      });
    }
  }

  public void handle(final GuildMemberUpdateRankPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true).ifPresent(
          guild -> this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getPlayerUniqueId())
              .ifPresent(user -> {
                final GuildMember member = guild.getGuildMember(user);
                if (member != null) {
                  guild.findGuildGroup(packet.getGroupUniqueId()).ifPresent(member::setGroup);
                }
              }));
    }
  }

  public void handle(final GuildMemberInviteAddPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getUniqueId()).ifPresent(
          user -> this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
              .filter(guild -> !guild.isMemberInvited(user))
              .ifPresent(guild -> guild.addMemberInvite(user, packet.getTime())));
    }
  }

  public void handle(final GuildMemberInviteRemovePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getUniqueId()).ifPresent(
          user -> this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
              .filter(guild -> guild.isMemberInvited(user))
              .ifPresent(guild -> guild.removeMemberInvite(user)));
    }
  }

  public void handle(final GuildCuboidSchematicSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      try {
        this.plugin.getSchematicFactory().reloadGuildSchematic(packet.getSchematicData());
      } catch (final IOException e) {
        this.plugin.getLogger().log(Level.WARNING, "Cannot reload guild schematic.", e);
      }
    }
  }

  public void handle(final GuildUserTeleportOutFromTerrainPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      final Player player = this.plugin.getServer().getPlayer(packet.getUniqueId());
      if (player != null) {
        this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
            .ifPresent(guild -> GuildHelper.teleportOutFromTerrain(player, guild));
      }
    }
  }

  public void handle(final GuildMemberRemovePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getUniqueId()).ifPresent(
          user -> this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
              .ifPresent(guild -> guild.removeGuildMember(user)));
    }
  }

  public void handle(final GuildUserCreatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && !this.plugin.getGuildUserFactory()
        .findUserByUniqueId(packet.getUniqueId()).isPresent()) {
      final GuildUser user = new GuildUser(packet.getUniqueId(), packet.getNickname());
      this.plugin.getGuildUserFactory().addUser(user);
    }
  }

  public void handle(final GuildHomeLocationUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true).ifPresent(
          guild -> guild.setHomeLocation(
              SerializeHelper.deserializeLocation(packet.getHomeLocation())));
    }
  }

  public void handle(final GuildUserCacheFighterPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getTargetUniqueId())
          .ifPresent(user -> user.cacheFighter(packet.getTargetUniqueId(), packet.getFightTime()));
    }
  }

  public void handle(final GuildJoinAlertMessageUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> guild.setJoinAlertMessage(packet.getJoinAlertMessage()));
    }
  }

  public void handle(final GuildAllyInviteEntryUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> guild.setAllyInviteEntry(packet.getAllyInviteEntry()));
    }
  }

  public void handle(final GuildRegionUpdateSizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> guild.getGuildRegion().setSize(packet.getSize()));
    }
  }

  public void handle(final GuildValidityTimeUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> guild.setValidityTime(packet.getValidityTime()));
    }
  }

  public void handle(final GuildUserCacheVictimPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getAttackerUniqueId())
          .ifPresent(user -> user.cacheVictim(packet.getVictimUniqueId(), packet.getTime()));
    }
  }

  public void handle(final GuildUserClearFightersPacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(packet.getUniqueId())
          .ifPresent(GuildUser::clearFighters);
    }
  }

  public void handle(final GuildPistonsUpdatePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      this.plugin.getGuildFactory().findGuildByCredential(packet.getGuildTag(), true)
          .ifPresent(guild -> guild.setPistonsOnGuild(packet.getPistons()));
    }
  }
}
