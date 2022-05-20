package pl.rosehc.actionbar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrioritizedActionBarPlugin extends JavaPlugin implements Listener {

  private static PrioritizedActionBarPlugin instance;
  private final PrioritizedActionBarFactory prioritizedActionBarFactory = new PrioritizedActionBarFactory();

  public static PrioritizedActionBarPlugin getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;
    this.getServer().getScheduler()
        .runTaskTimerAsynchronously(this, new PrioritizedActionBarUpdateTask(this), 1L, 1L);
    this.getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    this.prioritizedActionBarFactory.removeActionBars(event.getPlayer().getUniqueId());
  }

  public PrioritizedActionBarFactory getPrioritizedActionBarFactory() {
    return this.prioritizedActionBarFactory;
  }
}
