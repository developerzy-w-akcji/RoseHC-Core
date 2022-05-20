package pl.rosehc.achievements.achievement.reward;

import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.achievements.user.AchievementsUser;

public final class BoosterAchievementReward implements IAchievementReward {

  private final AchievementBoostType type;
  private final double amount;

  BoosterAchievementReward(final AchievementBoostType type, final double amount) {
    this.type = type;
    this.amount = amount;
  }

  @Override
  public void give(final AchievementsUser user) {
    user.setAchievementBoost(this.type, this.amount);
  }
}
