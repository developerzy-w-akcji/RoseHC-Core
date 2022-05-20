package pl.rosehc.achievements.commands;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.user.AchievementsUser;
import pl.rosehc.achievements.user.AchievementsUserProfileInventory;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformPlugin;

public final class ProfileCommand {

  private final AchievementsPlugin plugin;

  public ProfileCommand(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"profil",
      "statystyki"}, description = "Wyświetla GUI ze statystykami podanego gracza.", async = true)
  public void handleProfile(final @Sender Player player, final @Name("nickname") String nickname) {
    final AchievementsUser user = this.plugin.getAchievementsUserFactory()
        .findUserByNickname(nickname).orElseGet(() -> {
          try {
            return this.plugin.getAchievementsUserRepository().load(nickname);
          } catch (final SQLException ex) {
            this.plugin.getLogger()
                .log(Level.SEVERE, "Nie można było załadować danych gracza od osiągnięć.", ex);
            return null;
          }
        });
    if (Objects.isNull(user)) {
      throw new BladeExitMessage(ChatHelper.colored(PlatformPlugin.getInstance()
          .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
              player.getName())));
    }

    final AchievementsUserProfileInventory inventory = new AchievementsUserProfileInventory(player,
        user);
    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, inventory::open);
  }
}
