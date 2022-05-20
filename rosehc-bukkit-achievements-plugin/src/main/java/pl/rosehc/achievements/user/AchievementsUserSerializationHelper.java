package pl.rosehc.achievements.user;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.achievements.achievement.AchievementType;

public final class AchievementsUserSerializationHelper {

  private AchievementsUserSerializationHelper() {
  }

  public static Map<AchievementType, Double> deserializeStatisticMap(final String string) {
    if (Objects.isNull(string) || string.trim().isEmpty()) {
      return null;
    }

    final Map<AchievementType, Double> achievementStatisticMap = new ConcurrentHashMap<>();
    final String[] splitted = string.split(":");
    for (final String achievementStatistic : splitted) {
      final String[] achievementStatisticSplitted = achievementStatistic.split("@");
      if (achievementStatisticSplitted.length >= 2) {
        achievementStatisticMap.put(AchievementType.valueOf(achievementStatisticSplitted[0]),
            Double.parseDouble(achievementStatisticSplitted[1]));
      }
    }

    return achievementStatisticMap;
  }

  public static Map<AchievementBoostType, Double> deserializeBoostMap(final String string) {
    if (Objects.isNull(string) || string.trim().isEmpty()) {
      return null;
    }

    final Map<AchievementBoostType, Double> achievementBoostMap = new ConcurrentHashMap<>();
    final String[] splitted = string.split(":");
    for (final String achievementStatistic : splitted) {
      final String[] achievementStatisticSplitted = achievementStatistic.split("@");
      if (achievementStatisticSplitted.length >= 2) {
        achievementBoostMap.put(AchievementBoostType.valueOf(achievementStatisticSplitted[0]),
            Double.parseDouble(achievementStatisticSplitted[1]));
      }
    }

    return achievementBoostMap;
  }

  public static String serializeStatisticsMap(
      final Map<AchievementType, Double> achievementStatisticMap) {
    if (Objects.isNull(achievementStatisticMap) || achievementStatisticMap.isEmpty()) {
      return null;
    }

    final StringBuilder builder = new StringBuilder();
    for (final Entry<AchievementType, Double> entry : achievementStatisticMap.entrySet()) {
      builder.append(entry.getKey().name()).append("@").append(entry.getValue());
      builder.append(':');
    }

    return builder.toString();
  }

  public static String serializeBoostMap(
      final Map<AchievementBoostType, Double> achievementBoostMap) {
    if (Objects.isNull(achievementBoostMap) || achievementBoostMap.isEmpty()) {
      return null;
    }

    final StringBuilder builder = new StringBuilder();
    for (final Entry<AchievementBoostType, Double> entry : achievementBoostMap.entrySet()) {
      builder.append(entry.getKey().name()).append("@").append(entry.getValue());
      builder.append(':');
    }

    return builder.toString();
  }

  public static Set<String> deserializeCompletedAchievementSet(final String string) {
    if (Objects.isNull(string) || string.trim().isEmpty()) {
      return null;
    }

    final Set<String> completedAchievementSet = ConcurrentHashMap.newKeySet();
    completedAchievementSet.addAll(Arrays.asList(string.split(":")));
    return completedAchievementSet;
  }

  public static String serializeCompletedAchievementSet(final Set<String> completedAchievementSet) {
    return !Objects.isNull(completedAchievementSet) && !completedAchievementSet.isEmpty()
        ? String.join(":", completedAchievementSet) : null;
  }
}
