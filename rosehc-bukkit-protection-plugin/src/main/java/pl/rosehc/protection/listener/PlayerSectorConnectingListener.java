package pl.rosehc.protection.listener;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.adapter.helper.EventCompletionStage;
import pl.rosehc.protection.ProtectionPlugin;
import pl.rosehc.protection.user.ProtectionUser;
import pl.rosehc.sectors.sector.SectorConnectingEvent;

public final class PlayerSectorConnectingListener implements Listener {

  private final ProtectionPlugin plugin;

  public PlayerSectorConnectingListener(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onConnect(final SectorConnectingEvent event) {
    final EventCompletionStage completionStage = event.getCompletionStage();
    final Object waiter = new Object();
    completionStage.addWaiter(waiter);
    this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
      try {
        this.plugin.getProtectionUserFactory().findUser(event.getPlayer())
            .filter(ProtectionUser::isExpiryTimeChanged).ifPresent(user -> {
              try {
                this.plugin.getProtectionUserRepository().update(user);
              } catch (final SQLException ex) {
                this.plugin.getLogger()
                    .log(Level.SEVERE, "Nie można było wykonać update'u użytkownika.", ex);
              }
            });
      } finally {
        completionStage.removeWaiter(waiter);
      }
    });
  }
}
