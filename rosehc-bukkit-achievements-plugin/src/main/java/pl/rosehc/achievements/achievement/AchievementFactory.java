package pl.rosehc.achievements.achievement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import pl.rosehc.achievements.AchievementsConfiguration;
import pl.rosehc.achievements.AchievementsConfiguration.AchievementWrapper;
import pl.rosehc.achievements.achievement.reward.AchievementRewardCreator;

public final class AchievementFactory {

  private final Multimap<AchievementType, Achievement> achievementMultimap;

  public AchievementFactory(final AchievementsConfiguration configuration) {
    this.achievementMultimap = Multimaps.synchronizedMultimap(HashMultimap.create());
    for (final AchievementWrapper wrapper : configuration.achievementWrapperList) {
      final AchievementType type = wrapper.achievementTypeWrapper.toOriginal();
      this.achievementMultimap.put(type, new Achievement(type,
          wrapper.rewardList.stream().map(AchievementRewardCreator::create)
              .collect(Collectors.toList()), wrapper.requiredStatistics, wrapper.level));
    }
  }

  public List<Achievement> findAchievementListByType(final AchievementType type) {
    final Collection<Achievement> achievementCollection = this.achievementMultimap.get(type);
    return Objects.nonNull(achievementCollection) ? new ArrayList<>(achievementCollection)
        : new ArrayList<>();
  }

  public Multimap<AchievementType, Achievement> getAchievementMultimap() {
    return this.achievementMultimap;
  }
}
