package pl.rosehc.randomtp.linker.listeners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;
import pl.rosehc.actionbar.PrioritizedActionBarConstants;
import pl.rosehc.actionbar.PrioritizedActionBarPlugin;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.redis.callback.Callback;
import pl.rosehc.adapter.redis.callback.CallbackPacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserCombatTimeUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.randomtp.linker.LinkerRandomTPPlugin;
import pl.rosehc.randomtp.system.packet.SystemRandomTPArenaCreateRequestPacket;
import pl.rosehc.randomtp.system.packet.SystemRandomTPArenaCreateResponsePacket;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.ConnectHelper;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorType;

public final class GroupTeleportationListener implements Listener {

  private final LinkerRandomTPPlugin plugin;

  public GroupTeleportationListener(final LinkerRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInteract(final PlayerInteractEvent event) {
    final Player senderPlayer = event.getPlayer();
    if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !event.hasBlock()
        || !event.getClickedBlock().getType().equals(Material.WOOD_BUTTON)) {
      return;
    }

    final Block clickedBlock = event.getClickedBlock(), relativeBlock = clickedBlock.getRelative(
        ((Button) clickedBlock.getState().getData()).getAttachedFace());
    if (Objects.isNull(relativeBlock) || !relativeBlock.getType().equals(Material.NOTE_BLOCK)) {
      return;
    }

    if (!senderPlayer.getLocation().getBlock().getType().name().endsWith("PLATE")) {
      ChatHelper.sendMessage(senderPlayer,
          this.plugin.getRandomTPConfiguration().groupMessagesWrapper.youNeedToBePlate);
      return;
    }

    final Player nearestPlayer = this.getNearestPlayerToPlayer(senderPlayer);
    if (Objects.isNull(nearestPlayer)) {
      ChatHelper.sendMessage(senderPlayer,
          this.plugin.getRandomTPConfiguration().groupMessagesWrapper.cannotTeleportByYourself);
      return;
    }

    if (senderPlayer.getLocation().getBlock().getType().equals(Material.GOLD_PLATE)) {
      this.handleArenaTeleport(senderPlayer, nearestPlayer, event);
    } else {
      this.handleNormalTeleport(senderPlayer, nearestPlayer, event);
    }
  }

  private void handleArenaTeleport(final Player senderPlayer, final Player nearestPlayer,
      final PlayerInteractEvent event) {
    // TODO SPRAWDZANIE CZY TEPARKI SA OFF
    SectorsPlugin.getInstance().getSectorUserFactory()
        .findUserByUniqueId(senderPlayer.getUniqueId()).filter(user -> !user.isRedirecting())
        .ifPresent(senderUser -> SectorsPlugin.getInstance().getSectorUserFactory()
            .findUserByUniqueId(nearestPlayer.getUniqueId()).filter(user -> !user.isRedirecting())
            .ifPresent(nearestUser -> {
              final Optional<Sector> randomSectorOptional = SectorHelper.getRandomSector(
                  SectorType.GROUP_TELEPORTS);
              if (!randomSectorOptional.isPresent()) {
                ChatHelper.sendMessage(senderPlayer,
                    this.plugin.getRandomTPConfiguration().groupMessagesWrapper.noGroupTeleportsSectorFound);
                return;
              }

              final Sector sector = randomSectorOptional.get();
              senderUser.setRedirecting(true);
              nearestUser.setRedirecting(true);
              this.plugin.getRedisAdapter().sendPacket(
                  new SystemRandomTPArenaCreateRequestPacket(senderPlayer.getUniqueId(),
                      nearestPlayer.getUniqueId(),
                      SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getName()),
                  new Callback() {

                    @Override
                    public void done(final CallbackPacket packet) {
                      if (!senderPlayer.isOnline() || !nearestPlayer.isOnline()) {
                        return;
                      }

                      final SystemRandomTPArenaCreateResponsePacket responsePacket = (SystemRandomTPArenaCreateResponsePacket) packet;
                      final Location centerLocation = SerializeHelper.deserializeLocation(
                          responsePacket.getCenterLocation());
                      final long combatTime =
                          System.currentTimeMillis() + PlatformPlugin.getInstance()
                              .getPlatformConfiguration().parsedCombatTime;
                      ChatHelper.sendMessage(senderPlayer,
                          plugin.getRandomTPConfiguration().groupMessagesWrapper.teleportationSucceedArena.replace(
                                  "{SECTOR_NAME}", sector.getName())
                              .replace("{PLAYER_NAME}", nearestPlayer.getName()));
                      ChatHelper.sendMessage(nearestPlayer,
                          plugin.getRandomTPConfiguration().groupMessagesWrapper.teleportationSucceedArena.replace(
                                  "{SECTOR_NAME}", sector.getName())
                              .replace("{PLAYER_NAME}", senderPlayer.getName()));
                      PlatformPlugin.getInstance().getPlatformUserFactory()
                          .findUserByUniqueId(senderUser.getUniqueId())
                          .ifPresent(senderPlatformUser -> {
                            senderPlatformUser.setGroupTeleportsChange(true);
                            senderPlatformUser.setCombatTime(combatTime);
                            PlatformPlugin.getInstance().getRedisAdapter().sendPacket(
                                new PlatformUserCombatTimeUpdatePacket(
                                    senderPlatformUser.getUniqueId(),
                                    senderPlatformUser.getCombatTime()), "rhc_master_controller",
                                "rhc_platform");
                            PrioritizedActionBarPlugin.getInstance()
                                .getPrioritizedActionBarFactory()
                                .updateActionBar(nearestUser.getUniqueId(), ChatHelper.colored(
                                        PlatformPlugin.getInstance()
                                            .getPlatformConfiguration().messagesWrapper.combatLeftTimeInfo.replace(
                                                "{TIME}", TimeHelper.timeToString(
                                                    PlatformPlugin.getInstance()
                                                        .getPlatformConfiguration().parsedCombatTime))),
                                    PrioritizedActionBarConstants.ANTI_LOGOUT_ACTION_BAR_PRIORITY);
                          });
                      PlatformPlugin.getInstance().getPlatformUserFactory()
                          .findUserByUniqueId(nearestUser.getUniqueId())
                          .ifPresent(nearestPlatformUser -> {
                            nearestPlatformUser.setGroupTeleportsChange(true);
                            nearestPlatformUser.setCombatTime(combatTime);
                            PlatformPlugin.getInstance().getRedisAdapter().sendPacket(
                                new PlatformUserCombatTimeUpdatePacket(
                                    nearestPlatformUser.getUniqueId(),
                                    nearestPlatformUser.getCombatTime()), "rhc_master_controller",
                                "rhc_platform");
                            PrioritizedActionBarPlugin.getInstance()
                                .getPrioritizedActionBarFactory()
                                .updateActionBar(nearestUser.getUniqueId(), ChatHelper.colored(
                                        PlatformPlugin.getInstance()
                                            .getPlatformConfiguration().messagesWrapper.combatLeftTimeInfo.replace(
                                                "{TIME}", TimeHelper.timeToString(
                                                    PlatformPlugin.getInstance()
                                                        .getPlatformConfiguration().parsedCombatTime))),
                                    PrioritizedActionBarConstants.ANTI_LOGOUT_ACTION_BAR_PRIORITY);
                          });
                      ConnectHelper.connect(senderPlayer, senderUser, sector, centerLocation);
                      ConnectHelper.connect(nearestPlayer, nearestUser, sector, centerLocation);
                    }

                    @Override
                    public void error(final String ignored) {
                      if (!senderPlayer.isOnline() && !nearestPlayer.isOnline()) {
                        return;
                      }

                      final String formattedNoFreeArenasFoundMessage = plugin.getRandomTPConfiguration().groupMessagesWrapper.noFreeArenasFound.replace(
                          "{SECTOR_NAME}", sector.getName());
                      if (senderPlayer.isOnline()) {
                        ChatHelper.sendMessage(senderPlayer, formattedNoFreeArenasFoundMessage);
                        senderUser.setRedirecting(false);
                      }
                      if (nearestPlayer.isOnline()) {
                        ChatHelper.sendMessage(nearestPlayer, formattedNoFreeArenasFoundMessage);
                        nearestUser.setRedirecting(false);
                      }
                    }
                  }, "rhc_rtp_" + sector.getName());
            }));
  }

  private void handleNormalTeleport(final Player senderPlayer, final Player nearestPlayer,
      final PlayerInteractEvent event) {
    // TODO SPRAWDZANIE CZY TEPARKI SA OFF
    SectorsPlugin.getInstance().getSectorUserFactory()
        .findUserByUniqueId(senderPlayer.getUniqueId()).filter(user -> !user.isRedirecting())
        .flatMap(ignored1 -> SectorsPlugin.getInstance().getSectorUserFactory()
            .findUserByUniqueId(nearestPlayer.getUniqueId()).filter(user -> !user.isRedirecting()))
        .ifPresent(ignored2 -> {
          final Optional<Sector> randomSectorOptional = SectorHelper.getRandomSector(
              SectorType.GAME);
          if (!randomSectorOptional.isPresent()) {
            ChatHelper.sendMessage(senderPlayer,
                this.plugin.getRandomTPConfiguration().groupMessagesWrapper.noGuildSectorFound);
            ChatHelper.sendMessage(nearestPlayer,
                this.plugin.getRandomTPConfiguration().groupMessagesWrapper.noGuildSectorFound);
            return;
          }

          final Sector sector = randomSectorOptional.get();
          final Location randomLocation = sector.random();
          ChatHelper.sendMessage(senderPlayer,
              this.plugin.getRandomTPConfiguration().groupMessagesWrapper.teleportationSucceedNormal.replace(
                      "{SECTOR_NAME}", sector.getName())
                  .replace("{PLAYER_NAME}", nearestPlayer.getName())
                  .replace("{X}", String.valueOf(randomLocation.getBlockX()))
                  .replace("{Y}", String.valueOf(randomLocation.getBlockY()))
                  .replace("{Z}", String.valueOf(randomLocation.getBlockZ())));
          ChatHelper.sendMessage(nearestPlayer,
              this.plugin.getRandomTPConfiguration().groupMessagesWrapper.teleportationSucceedNormal.replace(
                      "{SECTOR_NAME}", sector.getName())
                  .replace("{PLAYER_NAME}", senderPlayer.getName())
                  .replace("{X}", String.valueOf(randomLocation.getBlockX()))
                  .replace("{Y}", String.valueOf(randomLocation.getBlockY()))
                  .replace("{Z}", String.valueOf(randomLocation.getBlockZ())));
          senderPlayer.teleport(randomLocation);
          nearestPlayer.teleport(randomLocation);
        });
  }

  private Player getNearestPlayerToPlayer(final Player senderPlayer) {
    final Location senderPlayerLocation = senderPlayer.getLocation();
    final List<Player> nearestPlayerList = new ArrayList<>();
    final int radius = this.plugin.getRandomTPConfiguration().groupTeleportRadius;
    for (final Player nearestPlayer : this.plugin.getServer().getOnlinePlayers()) {
      if (!nearestPlayer.equals(senderPlayer) && nearestPlayer.getLocation().getBlock().getType()
          .name().endsWith("_PLATE")
          && senderPlayerLocation.distance(nearestPlayer.getLocation()) <= radius) {
        nearestPlayerList.add(nearestPlayer.getPlayer());
      }
    }

    nearestPlayerList.sort(Comparator.comparingDouble(
        nearestPlayer -> senderPlayerLocation.distance(nearestPlayer.getLocation())));
    return !nearestPlayerList.isEmpty() ? nearestPlayerList.get(0) : null;
  }

  private boolean doesGuildCollide(final Location location) {
    try {
      Class.forName("pl.rosehc.guilds.GuildsPlugin");
      return GuildsPlugin.getInstance().getGuildFactory().findGuildInside(location).isPresent();
    } catch (final Exception ignored) {
      return false;
    }
  }
}
