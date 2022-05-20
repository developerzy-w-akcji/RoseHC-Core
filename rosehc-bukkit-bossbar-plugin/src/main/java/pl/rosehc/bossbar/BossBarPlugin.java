package pl.rosehc.bossbar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pl.rosehc.bossbar.user.UserBarFactory;

/**
 * @author stevimeister on 03/01/2022
 **/
public final class BossBarPlugin extends JavaPlugin implements Listener {

  private static BossBarPlugin instance;
  private UserBarFactory userBarFactory;

  public static BossBarPlugin getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;
    this.userBarFactory = new UserBarFactory();
    this.getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    this.userBarFactory.removeUserBar(event.getPlayer());
  }

  public UserBarFactory getUserBarFactory() {
    return this.userBarFactory;
  }
}
