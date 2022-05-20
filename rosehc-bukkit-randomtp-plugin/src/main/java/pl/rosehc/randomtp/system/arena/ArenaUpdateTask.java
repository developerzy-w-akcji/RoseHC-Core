package pl.rosehc.randomtp.system.arena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.entity.Player;
import pl.rosehc.actionbar.PrioritizedActionBarConstants;
import pl.rosehc.actionbar.PrioritizedActionBarPlugin;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.user.PlatformUserCombatTimeUpdatePacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.ConnectHelper;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorType;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class ArenaUpdateTask implements Runnable {

  private final SystemRandomTPPlugin plugin;

  public ArenaUpdateTask(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(this.plugin.getOriginal(), this, 20L, 20L);
  }

  @Override
  public void run() {
    final Set<Arena> arenasToDeleteSet = new HashSet<>();
    for (final Arena arena : this.plugin.getArenaFactory().getArenaMap().values()) {
      if (!arena.wasJustCreated()) {
        final Player senderPlayer = this.fetchPlayerFromArena(arena,
            this.plugin.getServer().getPlayer(arena.getSenderPlayerUniqueId()));
        final Player nearestPlayer = this.fetchPlayerFromArena(arena,
            this.plugin.getServer().getPlayer(arena.getNearestPlayerUniqueId()));
        if (senderPlayer == null && nearestPlayer == null) {
          arenasToDeleteSet.add(arena);
          continue;
        }

        if (senderPlayer == null || nearestPlayer == null) {
          final Player destinationPlayer = senderPlayer != null ? senderPlayer : nearestPlayer;
          if (arena.getDeletionTime() == 0L) {
            arena.prepareForDeletion();
            PlatformPlugin.getInstance().getPlatformUserFactory()
                .findUserByUniqueId(destinationPlayer.getUniqueId()).ifPresent(user -> {
                  user.setCombatTime(0L);
                  PlatformPlugin.getInstance().getRedisAdapter()
                      .sendPacket(new PlatformUserCombatTimeUpdatePacket(user.getUniqueId(), 0L),
                          "rhc_master_controller", "rhc_platform");
                });
          }

          if (arena.getDeletionTime() > System.currentTimeMillis()) {
            PrioritizedActionBarPlugin.getInstance().getPrioritizedActionBarFactory()
                .updateActionBar(destinationPlayer.getUniqueId(), ChatHelper.colored(
                        this.plugin.getRandomTPConfiguration().arenaDeletionActionBarInfo.replace(
                            "{TIME}", TimeHelper.timeToString(
                                arena.getDeletionTime() - System.currentTimeMillis()))),
                    PrioritizedActionBarConstants.ARENA_DELETION_INFO_ACTION_BAR_PRIORITY);
            continue;
          }

          final Optional<SectorUser> sectorUserOptional = SectorsPlugin.getInstance()
              .getSectorUserFactory().findUserByUniqueId(destinationPlayer.getUniqueId());
          final Sector sector = SectorHelper.getRandomSector(SectorType.SPAWN)
              .orElse(SectorHelper.getRandomSector(SectorType.GAME).orElse(null));
          if (Objects.isNull(sector) || !sectorUserOptional.isPresent()) {
            this.plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(this.plugin.getOriginal(),
                    () -> destinationPlayer.kickPlayer(ChatHelper.colored(
                        this.plugin.getRandomTPConfiguration().cannotConnectToSector)));
            continue;
          }

          sectorUserOptional.filter(user -> !user.isRedirecting()).ifPresent(user -> {
            user.setRedirecting(true);
            ConnectHelper.connect(destinationPlayer, user, sector,
                sector.getType().equals(SectorType.SPAWN) ? PlatformPlugin.getInstance()
                    .getPlatformConfiguration().spawnLocationWrapper.unwrap() : sector.random(),
                () -> {
                }, () -> this.plugin.getServer().getScheduler()
                    .scheduleSyncDelayedTask(this.plugin.getOriginal(),
                        () -> destinationPlayer.kickPlayer(ChatHelper.colored(
                            this.plugin.getRandomTPConfiguration().cannotConnectToSector))));
            arenasToDeleteSet.add(arena);
          });
        }
      }
    }

    if (!arenasToDeleteSet.isEmpty()) {
      for (final Arena arena : arenasToDeleteSet) {
        final ArenaDeletionTask deletionTask = new ArenaDeletionTask(
            new ArrayList<>(arena.getPlacedBlockLocationMap().values()));
        deletionTask.start();
      }

      this.plugin.getArenaFactory().getArenaMap().entrySet()
          .removeIf(entry -> arenasToDeleteSet.contains(entry.getValue()));
    }
  }

  private Player fetchPlayerFromArena(final Arena arena, final Player player) {
    if (player != null) {
      final Optional<Arena> otherArenaOptional = this.plugin.getArenaFactory()
          .findArenaByPlayer(player);
      if (otherArenaOptional.filter(otherArena -> !otherArena.equals(arena) && (
          otherArena.getSenderPlayerUniqueId().equals(player.getUniqueId())
              || otherArena.getNearestPlayerUniqueId().equals(player.getUniqueId()))).isPresent()
          || !otherArenaOptional.isPresent()) {
        return null;
      }
    }

    return player;
  }
}
