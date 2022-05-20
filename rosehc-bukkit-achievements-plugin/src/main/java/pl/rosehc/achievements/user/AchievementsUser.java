package pl.rosehc.achievements.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import pl.rosehc.achievements.achievement.Achievement;
import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.achievements.achievement.AchievementType;

public final class AchievementsUser {

  private final UUID uniqueId;
  private Map<AchievementType, Double> achievementStatisticMap;
  private Map<AchievementBoostType, Double> achievementBoostMap;
  private Set<String> completedAchievementSet;
  private String nickname;
  private long lastTimeMeasurement;
  private boolean needUpdate;

  public AchievementsUser(final UUID uniqueId, final String nickname) {
    this.uniqueId = uniqueId;
    this.nickname = nickname;
  }

  public AchievementsUser(final ResultSet result) throws SQLException {
    this.uniqueId = UUID.fromString(result.getString("uniqueId"));
    this.nickname = result.getString("nickname");
    this.achievementStatisticMap = AchievementsUserSerializationHelper.deserializeStatisticMap(
        result.getString("achievementStatistics"));
    this.achievementBoostMap = AchievementsUserSerializationHelper.deserializeBoostMap(
        result.getString("achievementBoosts"));
    this.completedAchievementSet = AchievementsUserSerializationHelper.deserializeCompletedAchievementSet(
        result.getString("completedAchievements"));
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public String getNickname() {
    return this.nickname;
  }

  public void setNickname(final String nickname) {
    this.nickname = nickname;
    this.needUpdate = true;
  }

  public Map<AchievementType, Double> getAchievementStatisticMap() {
    return this.achievementStatisticMap;
  }

  public double getAchievementStatistic(final AchievementType type) {
    if (Objects.isNull(this.achievementStatisticMap)) {
      return 0D;
    }

    return this.achievementStatisticMap.getOrDefault(type, 0D);
  }

  public void incrementAchievementStatistic(final AchievementType type,
      final double incrementalValue) {
    if (Objects.isNull(this.achievementStatisticMap)) {
      this.achievementStatisticMap = new ConcurrentHashMap<>();
    }

    final double value = this.achievementStatisticMap.getOrDefault(type, 0D);
    this.achievementStatisticMap.put(type, value + incrementalValue);
    this.needUpdate = true;
  }

  public Map<AchievementBoostType, Double> getAchievementBoostMap() {
    return this.achievementBoostMap;
  }

  public double getAchievementBoost(final AchievementBoostType type) {
    if (Objects.isNull(this.achievementBoostMap)) {
      return -1;
    }

    final Double boost = this.achievementBoostMap.get(type);
    return Objects.nonNull(boost) ? boost : -1;
  }

  public void setAchievementBoost(final AchievementBoostType type, final double boost) {
    if (Objects.isNull(this.achievementBoostMap)) {
      this.achievementBoostMap = new ConcurrentHashMap<>();
    }

    this.achievementBoostMap.put(type, boost);
    this.needUpdate = false;
  }

  public Set<String> getCompletedAchievementSet() {
    return this.completedAchievementSet;
  }

  public boolean completeAchievement(final Achievement achievement) {
    if (Objects.isNull(this.completedAchievementSet)) {
      this.completedAchievementSet = ConcurrentHashMap.newKeySet();
    }

    final String achievementString = achievement.toString();
    final boolean completed = this.completedAchievementSet.add(achievementString);
    if (completed) {
      this.needUpdate = true;
    }

    return completed;
  }

  public boolean hasCompletedAchievement(final Achievement achievement) {
    if (Objects.isNull(this.completedAchievementSet)) {
      return false;
    }

    final String achievementString = achievement.toString();
    return this.completedAchievementSet.contains(achievementString);
  }

  public boolean isNeedUpdate() {
    final boolean needUpdate = this.needUpdate;
    this.needUpdate = false;
    return needUpdate;
  }

  public long getLastTimeMeasurement() {
    return this.lastTimeMeasurement;
  }

  public void setLastTimeMeasurement(final long lastTimeMeasurement) {
    this.lastTimeMeasurement = lastTimeMeasurement;
  }
}
