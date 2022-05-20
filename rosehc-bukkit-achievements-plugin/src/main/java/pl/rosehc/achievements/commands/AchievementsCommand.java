package pl.rosehc.achievements.commands;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.inventory.AchievementMainInventory;

public final class AchievementsCommand {

  private final AchievementsPlugin plugin;

  public AchievementsCommand(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"achievements", "osiagniecia",
      "osg"}, description = "Otwiera GUI od osiągnięć gracza.")
  public void handleAchievements(final @Sender Player player) {
    final AchievementMainInventory inventory = new AchievementMainInventory(player);
    inventory.open();
  }
}
