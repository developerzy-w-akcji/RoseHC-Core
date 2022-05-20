package pl.rosehc.achievements.achievement.reward;

import org.bukkit.entity.Player;
import pl.rosehc.achievements.user.AchievementsUser;

public interface IAchievementReward {

  default void give(final Player player) {
  }

  default void give(final AchievementsUser user) {
  }
}
