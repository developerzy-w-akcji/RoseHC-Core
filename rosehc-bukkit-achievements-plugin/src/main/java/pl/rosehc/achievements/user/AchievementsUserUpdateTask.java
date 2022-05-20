package pl.rosehc.achievements.user;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import pl.rosehc.achievements.AchievementsPlugin;

public final class AchievementsUserUpdateTask implements Runnable {

  private final AchievementsPlugin plugin;

  public AchievementsUserUpdateTask(final AchievementsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(this.plugin, this, 1200L, 1200L);
  }

  @Override
  public void run() {
    final List<AchievementsUser> userList = this.plugin.getAchievementsUserFactory().getUserMap()
        .values().stream().filter(AchievementsUser::isNeedUpdate).collect(Collectors.toList());
    if (userList.size() != 0) {
      try {
        this.plugin.getAchievementsUserRepository().updateAll(userList);
      } catch (final SQLException ex) {
        this.plugin.getLogger()
            .log(Level.WARNING, "Nie można było zapisać użytkowników do bazy danych.", ex);
      }
    }
  }
}
