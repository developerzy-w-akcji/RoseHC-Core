package pl.rosehc.protection.user.task;

import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.protection.ProtectionPlugin;

public final class ProtectionUserExpiryTask implements Runnable {

  private final ProtectionPlugin plugin;

  public ProtectionUserExpiryTask(final ProtectionPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this, 10L, 10L);
  }

  @Override
  public void run() {
    for (final Player player : Bukkit.getOnlinePlayers()) {
      this.plugin.getProtectionUserFactory().findUser(player).ifPresent(user -> {
        if (PlatformPlugin.getInstance().getPlatformConfiguration().serverFreezeState) {
          user.setExpiryTime(System.currentTimeMillis()
              + this.plugin.getProtectionConfiguration().parsedExpiryTime);
        }

        if (user.hasExpired() && user.getExpiryTime() != 0L) {
          user.setExpiryTime(0L);
          ChatHelper.sendMessage(player,
              this.plugin.getProtectionConfiguration().protectionHasExpired);
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
}
