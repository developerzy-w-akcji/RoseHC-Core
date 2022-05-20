package pl.rosehc.achievements.achievement;

import java.util.List;
import java.util.Objects;
import pl.rosehc.achievements.achievement.reward.IAchievementReward;
import pl.rosehc.adapter.helper.TimeHelper;

public final class Achievement {

  private final AchievementType type;
  private final List<IAchievementReward> rewardList;
  private final double requiredStatistics;
  private final int level;

  public Achievement(final AchievementType type, final List<IAchievementReward> rewardList,
      final String requiredStatistics, final int level) {
    this.type = type;
    this.rewardList = rewardList;
    this.requiredStatistics =
        type.equals(AchievementType.SPEND_TIME) ? TimeHelper.timeFromString(requiredStatistics)
            : Double.parseDouble(requiredStatistics);
    this.level = level;
  }

  public AchievementType getType() {
    return this.type;
  }

  public List<IAchievementReward> getRewardList() {
    return this.rewardList;
  }

  public double getRequiredStatistics() {
    return this.requiredStatistics;
  }

  public int getLevel() {
    return this.level;
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || this.getClass() != object.getClass()) {
      return false;
    }

    final Achievement that = (Achievement) object;
    return this.level == that.level && this.type == that.type;
  }

  @Override
  public String toString() {
    return this.type.name() + ":" + this.level;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.type, this.level);
  }
}
