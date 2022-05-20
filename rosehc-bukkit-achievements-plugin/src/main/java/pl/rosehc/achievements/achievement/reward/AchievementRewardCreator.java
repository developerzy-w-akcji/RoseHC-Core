package pl.rosehc.achievements.achievement.reward;

import java.util.Objects;
import pl.rosehc.achievements.AchievementsConfiguration.AchievementRewardWrapper;

public final class AchievementRewardCreator {

  private AchievementRewardCreator() {
  }

  public static IAchievementReward create(final AchievementRewardWrapper wrapper) {
    return Objects.isNull(wrapper.rewardItemWrapper) ? new BoosterAchievementReward(
        wrapper.boostTypeWrapper.toOriginal(), wrapper.boostAmount)
        : new ItemStackAchievementReward(wrapper.rewardItemWrapper.asItemStack());
  }
}
