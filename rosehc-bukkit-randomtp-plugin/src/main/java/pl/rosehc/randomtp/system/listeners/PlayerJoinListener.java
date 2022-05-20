package pl.rosehc.randomtp.system.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class PlayerJoinListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerJoinListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    final Player sender = event.getPlayer();
    this.plugin.getArenaFactory().findArenaByPlayer(sender).ifPresent(arena -> {
      for (final Player receiver : this.plugin.getServer().getOnlinePlayers()) {
        if (!receiver.equals(sender)) {
          if (!this.plugin.getArenaFactory().findArenaByPlayer(receiver).filter(
                  receiverArena -> receiverArena.getSenderPlayerUniqueId().equals(sender.getUniqueId())
                      || receiverArena.getNearestPlayerUniqueId().equals(sender.getUniqueId()))
              .isPresent()) {
            receiver.hidePlayer(sender);
          }
          if (!arena.getSenderPlayerUniqueId().equals(receiver.getUniqueId())
              && !arena.getNearestPlayerUniqueId().equals(receiver.getUniqueId())) {
            sender.hidePlayer(receiver);
          }
        }
      }

      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin.getOriginal(),
          () -> arena.getArenaRegion().sendBorder(sender), 2L);
    });
  }
}
