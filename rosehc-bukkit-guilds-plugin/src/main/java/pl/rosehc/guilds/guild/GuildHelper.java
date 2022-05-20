package pl.rosehc.guilds.guild;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.controller.packet.platform.user.PlatformUserCombatTimeUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.group.GuildGroup;
import pl.rosehc.guilds.guild.group.GuildGroupFactory;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.ConnectHelper;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorType;
import pl.rosehc.waypoint.WaypointConstants;
import pl.rosehc.waypoint.WaypointHelper;

public final class GuildHelper {

  private GuildHelper() {
  }

  public static Map<UUID, GuildGroup> createDefaultGuildGroups() {
    final Map<UUID, GuildGroup> defaultGuildGroupMap = new ConcurrentHashMap<>();
    final GuildGroupFactory guildGroupFactory = GuildsPlugin.getInstance().getGuildGroupFactory();
    defaultGuildGroupMap.put(guildGroupFactory.getDefaultGuildGroup().getUniqueId(),
        new GuildGroup(guildGroupFactory.getDefaultGuildGroup()));
    defaultGuildGroupMap.put(guildGroupFactory.getLeaderGuildGroup().getUniqueId(),
        new GuildGroup(guildGroupFactory.getLeaderGuildGroup()));
    defaultGuildGroupMap.put(guildGroupFactory.getDeputyGuildGroup().getUniqueId(),
        new GuildGroup(guildGroupFactory.getDeputyGuildGroup()));
    for (final GuildGroup group : guildGroupFactory.getDefaultGuildGroupMap().values()) {
      if (!defaultGuildGroupMap.containsKey(group.getUniqueId())) {
        defaultGuildGroupMap.put(group.getUniqueId(), new GuildGroup(group));
      }
    }

    return defaultGuildGroupMap;
  }

  public static void updateGuildHelpWaypoint(final GuildPlayerHelpInfo helpInfo, final Guild guild,
      final boolean ally) {
    final Location location = new Location(Bukkit.getWorlds().get(0), helpInfo.getX(),
        helpInfo.getY() + 2, helpInfo.getZ());
    final String waypointTitle = GuildsPlugin.getInstance()
        .getGuildsConfiguration().messagesWrapper.guildHelpWaypointTitle.replace("{PLAYER_NAME}",
            helpInfo.getNickname()).replace("{X}", String.valueOf(helpInfo.getX()))
        .replace("{Y}", String.valueOf(helpInfo.getY()))
        .replace("{Z}", String.valueOf(helpInfo.getZ()));
    final Color waypointColor = ally ? WaypointConstants.NEED_HELP_ALLY_WAYPOINT_COLOR
        : WaypointConstants.NEED_HELP_GUILD_WAYPOINT_COLOR;
    final String waypointAssetSha = ally ? WaypointConstants.NEED_HELP_ALLY_WAYPOINT_ASSET_SHA
        : WaypointConstants.NEED_HELP_GUILD_WAYPOINT_ASSET_SHA;
    final int waypointAssetId = ally ? WaypointConstants.NEED_HELP_ALLY_WAYPOINT_ASSET_ID
        : WaypointConstants.NEED_HELP_GUILD_WAYPOINT_ASSET_ID;
    final int waypointId = helpInfo.getWaypointId();
    for (final GuildMember member : guild.getGuildMembers()) {
      if (member != null) {
        final Player player = Bukkit.getPlayer(member.getUniqueId());
        if (player != null) {
          WaypointHelper.deleteWaypoint(player, waypointId);
          WaypointHelper.createWaypoint(player, location, waypointId, waypointTitle, waypointColor,
              waypointAssetSha, waypointAssetId, GuildsPlugin.getInstance()
                  .getGuildsConfiguration().pluginWrapper.parsedGuildNeedHelpWaypointTime);
        }
      }
    }

    if (ally && guild.getAlliedGuild() != null) {
      for (final GuildMember member : guild.getAlliedGuild().getGuildMembers()) {
        if (member != null) {
          final Player player = Bukkit.getPlayer(member.getUniqueId());
          if (player != null) {
            WaypointHelper.deleteWaypoint(player, waypointId);
            WaypointHelper.createWaypoint(player, location, waypointId, waypointTitle,
                waypointColor, waypointAssetSha, waypointAssetId, GuildsPlugin.getInstance()
                    .getGuildsConfiguration().pluginWrapper.parsedGuildNeedHelpWaypointTime);
          }
        }
      }
    }
  }

  public static void teleportOutFromTerrain(final Player player, final Guild guild) {
    if (guild.getGuildRegion().isInside(player.getLocation())) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getInstance(),
          () -> SectorsPlugin.getInstance().getSectorUserFactory()
              .findUserByUniqueId(player.getUniqueId()).ifPresent(sectorUser -> {
                sectorUser.setRedirecting(true);
                PlatformPlugin.getInstance().getPlatformUserFactory()
                    .findUserByUniqueId(player.getUniqueId()).ifPresent(platformUser -> {
                      platformUser.setCombatTime(0L);
                      PlatformPlugin.getInstance().getRedisAdapter().sendPacket(
                          new PlatformUserCombatTimeUpdatePacket(platformUser.getUniqueId(), 0L),
                          "rhc_master_controller", "rhc_platform");
                    });
                Sector sector = SectorHelper.getRandomSector(SectorType.SPAWN).orElse(
                    SectorHelper.getRandomSector(SectorType.GAME, ignored -> true).orElse(null));
                if (sector == null) {
                  player.teleport(
                      SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().random());
                  return;
                }

                ConnectHelper.connect(player, sectorUser, sector,
                    sector.getType().equals(SectorType.SPAWN) ? PlatformPlugin.getInstance()
                        .getPlatformConfiguration().spawnLocationWrapper.unwrap() : sector.random(),
                    () -> {
                    }, () -> player.teleport(
                        SectorsPlugin.getInstance().getSectorFactory().getCurrentSector()
                            .random()));
              }), 4L);
    }
  }

  public static void removeGuildHelpWaypoint(final Guild guild, final GuildPlayerHelpInfo info,
      final boolean ally) {
    final int waypointId = info.getWaypointId();
    for (final GuildMember member : guild.getGuildMembers()) {
      if (member != null) {
        final Player player = Bukkit.getPlayer(member.getUniqueId());
        if (player != null) {
          WaypointHelper.deleteWaypoint(player, waypointId);
        }
      }
    }

    if (ally && guild.getAlliedGuild() != null) {
      for (final GuildMember member : guild.getAlliedGuild().getGuildMembers()) {
        if (member != null) {
          final Player player = Bukkit.getPlayer(member.getUniqueId());
          if (player != null) {
            WaypointHelper.deleteWaypoint(player, waypointId);
          }
        }
      }
    }
  }
}
