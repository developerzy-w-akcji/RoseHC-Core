package pl.rosehc.protection.listener;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pl.rosehc.protection.ProtectionPlugin;
import pl.rosehc.protection.user.ProtectionUser;
import pl.rosehc.sectors.SectorsPlugin;

public final class PlayerPreLoginListener implements Listener {

  private final ProtectionPlugin plugin;

  public PlayerPreLoginListener(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onLogin(final AsyncPlayerPreLoginEvent event) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      try {
        final ProtectionUser user = this.plugin.getProtectionUserRepository()
            .load(event.getUniqueId());
        this.plugin.getProtectionUserFactory().removeUser(user);
        this.plugin.getProtectionUserFactory().addUser(user);
      } catch (final SQLException ex) {
        this.plugin.getLogger()
            .log(Level.SEVERE, "Nie można było załadować danych gracza od ochrony.", ex);
      }
    }
  }
}
