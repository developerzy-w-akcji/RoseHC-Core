package pl.rosehc.actionbar;

import org.bukkit.entity.Player;

public final class PrioritizedActionBarUpdateTask implements Runnable {

  private final PrioritizedActionBarPlugin plugin;

  public PrioritizedActionBarUpdateTask(final PrioritizedActionBarPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getPrioritizedActionBarFactory().sendActionBars(player);
    }
  }
}
