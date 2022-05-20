package pl.rosehc.achievements.listener.player;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.user.AchievementsUser;
import pl.rosehc.sectors.SectorsPlugin;

public final class PlayerPreLoginListener implements Listener {

  private final AchievementsPlugin plugin;

  public PlayerPreLoginListener(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLogin(final AsyncPlayerPreLoginEvent event) {
    if (SectorsPlugin.getInstance().isLoaded()) {
      try {
        AchievementsUser user = this.plugin.getAchievementsUserRepository()
            .load(event.getUniqueId());
        if (Objects.isNull(user)) {
          user = new AchievementsUser(event.getUniqueId(), event.getName());
          this.plugin.getAchievementsUserRepository().insert(user);
        }

        this.plugin.getAchievementsUserFactory().addUser(user);
      } catch (final SQLException ex) {
        this.plugin.getLogger()
            .log(Level.SEVERE, "Nie można było załadować danych gracza od osiągnięć.", ex);
      }
    }
  }
}
