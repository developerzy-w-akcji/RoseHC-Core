package pl.rosehc.guilds.listener.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import pl.rosehc.guilds.GuildsPlugin;

public final class BlockCenterProtectionListener implements Listener {

  private final GuildsPlugin plugin;

  public BlockCenterProtectionListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPistonExtend(final BlockPistonExtendEvent event) {
    for (final Block block : event.getBlocks()) {
      final Location location = block.getLocation();
      if (this.plugin.getGuildFactory().findGuildInside(location)
          .filter(guild -> guild.getGuildRegion().isInsideCenter(location) || guild.getGuildRegion()
              .getCenterLocation().equals(location)).isPresent()) {
        event.setCancelled(true);
        break;
      }
    }
  }

  @EventHandler
  public void onPistonRetract(final BlockPistonRetractEvent event) {
    for (final Block block : event.getBlocks()) {
      final Location location = block.getLocation();
      if (this.plugin.getGuildFactory().findGuildInside(location).filter(
          guild -> guild.getGuildRegion().isInsideCenter(location) || guild.getGuildRegion()
              .getCenterLocation().equals(location)).isPresent()) {
        event.setCancelled(true);
        break;
      }
    }
  }
}
