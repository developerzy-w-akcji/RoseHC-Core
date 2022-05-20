package pl.rosehc.randomtp.system.arena;

import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.ConnectHelper;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorType;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class ArenaDisconnectGhostPlayersTask implements Runnable {

  private final SystemRandomTPPlugin plugin;

  public ArenaDisconnectGhostPlayersTask(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(this.plugin.getOriginal(), this, 60L, 60L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      if (!this.plugin.getArenaFactory().findArenaByPlayer(player).isPresent()) {
        final Optional<SectorUser> sectorUserOptional = SectorsPlugin.getInstance()
            .getSectorUserFactory().findUserByUniqueId(player.getUniqueId());
        final Sector sector = SectorHelper.getRandomSector(SectorType.SPAWN)
            .orElse(SectorHelper.getRandomSector(SectorType.GAME).orElse(null));
        if (Objects.isNull(sector) || !sectorUserOptional.isPresent()) {
          this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin.getOriginal(),
              () -> player.kickPlayer(ChatHelper.colored(
                  this.plugin.getRandomTPConfiguration().cannotConnectToSector)));
          continue;
        }

        sectorUserOptional.filter(user -> !user.isRedirecting()).ifPresent(user -> {
          user.setRedirecting(true);
          ConnectHelper.connect(player, user, sector,
              sector.getType().equals(SectorType.SPAWN) ? PlatformPlugin.getInstance()
                  .getPlatformConfiguration().spawnLocationWrapper.unwrap() : sector.random(),
              () -> {
              }, () -> this.plugin.getServer().getScheduler()
                  .scheduleSyncDelayedTask(this.plugin.getOriginal(), () -> player.kickPlayer(
                      ChatHelper.colored(
                          this.plugin.getRandomTPConfiguration().cannotConnectToSector))));
        });
      }
    }
  }
}
