package pl.rosehc.randomtp.linker.listeners;

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
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.randomtp.linker.LinkerRandomTPPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.SectorType;

public final class SoloTeleportationListener implements Listener {

  private final LinkerRandomTPPlugin plugin;

  public SoloTeleportationListener(final LinkerRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInteract(final PlayerInteractEvent event) {
    final Player player = event.getPlayer();
    if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !event.hasBlock()
        || !event.getClickedBlock().getType().equals(Material.STONE_BUTTON)) {
      return;
    }

    final Block clickedBlock = event.getClickedBlock(), relativeBlock = clickedBlock.getRelative(
        ((Button) clickedBlock.getState().getData()).getAttachedFace());
    if (Objects.isNull(relativeBlock) || !relativeBlock.getType().equals(Material.SPONGE)) {
      return;
    }

    SectorsPlugin.getInstance().getSectorUserFactory().findUserByUniqueId(player.getUniqueId())
        .filter(user -> !user.isRedirecting()).ifPresent(ignored -> {
          final Optional<Sector> randomSectorOptional = SectorHelper.getRandomSector(SectorType.GAME);
          if (!randomSectorOptional.isPresent()) {
            ChatHelper.sendMessage(player,
                this.plugin.getRandomTPConfiguration().soloMessagesWrapper.noGuildSectorFound);
            return;
          }

          final Sector sector = randomSectorOptional.get();
          final Location randomLocation = sector.random();
          if (this.doesGuildCollide(randomLocation)) {
            ChatHelper.sendMessage(player,
                this.plugin.getRandomTPConfiguration().soloMessagesWrapper.noFreeLocationWasFound);
            return;
          }

          ChatHelper.sendMessage(player,
              this.plugin.getRandomTPConfiguration().soloMessagesWrapper.teleportationSucceed.replace(
                      "{SECTOR_NAME}", sector.getName())
                  .replace("{X}", String.valueOf(randomLocation.getBlockX()))
                  .replace("{Y}", String.valueOf(randomLocation.getBlockY()))
                  .replace("{Z}", String.valueOf(randomLocation.getBlockZ())));
          player.teleport(randomLocation);
        });
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
