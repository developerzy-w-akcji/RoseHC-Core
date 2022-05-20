package pl.rosehc.guilds.listener.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.SectorType;

public final class BlockFromToListener implements Listener {

  private final GuildsPlugin plugin;

  public BlockFromToListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onFromTo(final BlockFromToEvent event) {
    if (!SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getType()
        .equals(SectorType.GAME)) {
      return;
    }

    final Block fromBlock = event.getBlock();
    if (!fromBlock.isLiquid()) {
      return;
    }

    final Location fromBlockLocation = fromBlock.getLocation();
    final Location toBlockLocation = event.getToBlock().getLocation();
    if (!this.plugin.getGuildFactory().findGuildInside(fromBlockLocation).isPresent()
        || !this.plugin.getGuildFactory().findGuildNear(toBlockLocation).isPresent()
        || PlatformPlugin.getInstance().getVanishingBlockFactory()
        .isVanished(fromBlockLocation, Material.STATIONARY_WATER) || PlatformPlugin.getInstance()
        .getVanishingBlockFactory().isVanished(toBlockLocation, Material.STATIONARY_WATER)) {
      event.setCancelled(true);
    }
  }
}
