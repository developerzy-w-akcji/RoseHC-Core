package pl.rosehc.randomtp.system.listeners;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.randomtp.system.arena.ArenaDeletionTask;

public final class PlayerQuitListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerQuitListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    this.plugin.getArenaFactory().findArenaByPlayer(player).ifPresent(arena -> {
      this.plugin.getArenaFactory().removeArena(player.getUniqueId());
      if (!this.plugin.getArenaFactory().getArenaMap().containsKey(arena.getSenderPlayerUniqueId())
          && !this.plugin.getArenaFactory().getArenaMap()
          .containsKey(arena.getNearestPlayerUniqueId())) {
        final ArenaDeletionTask deletionTask = new ArenaDeletionTask(
            new ArrayList<>(arena.getPlacedBlockLocationMap().values()));
        deletionTask.start();
      }
    });
  }
}
