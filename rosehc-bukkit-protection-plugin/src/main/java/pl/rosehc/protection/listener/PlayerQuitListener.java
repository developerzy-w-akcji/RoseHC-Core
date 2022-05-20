package pl.rosehc.protection.listener;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.rosehc.protection.ProtectionPlugin;

public final class PlayerQuitListener implements Listener {

  private final ProtectionPlugin plugin;

  public PlayerQuitListener(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(final PlayerQuitEvent event) {
    this.plugin.getProtectionUserFactory().findUser(event.getPlayer()).ifPresent(user -> {
      this.plugin.getProtectionUserFactory().removeUser(user);
      if (user.isExpiryTimeChanged()) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
          try {
            this.plugin.getProtectionUserRepository().update(user);
          } catch (final SQLException ex) {
            this.plugin.getLogger()
                .log(Level.SEVERE, "Nie można było wykonać update'u użytkownika.", ex);
          }
        });
      }
    });
  }
}
