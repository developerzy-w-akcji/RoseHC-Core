package pl.rosehc.achievements.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import pl.rosehc.adapter.database.DatabaseAdapter;
import pl.rosehc.adapter.database.DatabaseReference;
import pl.rosehc.adapter.database.DatabaseRepository;

public final class AchievementsUserRepository extends DatabaseRepository<UUID, AchievementsUser> {

  public AchievementsUserRepository(final DatabaseAdapter databaseAdapter) throws SQLException {
    super(databaseAdapter);
  }

  @Override
  public Map<UUID, AchievementsUser> loadAll() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public AchievementsUser load(final UUID uniqueId) throws SQLException {
    final DatabaseReference<AchievementsUser> databaseReference = new DatabaseReference<>();
    this.doSelect("SELECT * FROM rhc_achievements_users WHERE uniqueId = ?",
        statement -> statement.setString(1, uniqueId.toString()),
        result -> databaseReference.set(new AchievementsUser(result)));
    return databaseReference.get();
  }

  @Override
  public void prepareTable() throws SQLException {
    this.consumeConnection(connection -> {
      try (final Statement statement = connection.createStatement()) {
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS rhc_achievements_users (uniqueId CHAR(36) PRIMARY KEY NOT NULL, nickname VARCHAR(16) NOT NULL,"
                + " achievementStatistics TEXT, achievementBoosts TEXT, completedAchievements TEXT);");
      }
    });
  }

  @Override
  public void insert(final AchievementsUser user) throws SQLException {
    this.doUpdate(
        "INSERT INTO rhc_achievements_users (uniqueId, nickname, achievementStatistics, achievementBoosts, completedAchievements)"
            + " VALUES (?, ?, ?, ?, ?)",
        statement -> {
          statement.setString(1, user.getUniqueId().toString());
          statement.setString(2, user.getNickname());
          statement.setString(3, null);
          statement.setString(4, null);
          statement.setString(5, null);
        });
  }

  @Override
  public void update(final AchievementsUser user) throws SQLException {
    this.doUpdate(
        "UPDATE rhc_achievements_users SET nickname = ?, achievementStatistics = ?, achievementBoosts = ?,"
            + " completedAchievements = ? WHERE uniqueId = ?",
        statement -> this.setStatementParameters(statement, user));
  }

  @Override
  public void updateAll(final Collection<AchievementsUser> userCollection) throws SQLException {
    this.doUpdate(
        "UPDATE rhc_achievements_users SET nickname = ?, achievementStatistics = ?, achievementBoosts = ?,"
            + " completedAchievements = ? WHERE uniqueId = ?",
        true, statement -> {
          for (final AchievementsUser user : userCollection) {
            this.setStatementParameters(statement, user);
            statement.addBatch();
          }
        });
  }

  @Override
  public void delete(final AchievementsUser ignored) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void deleteAll(final Collection<AchievementsUser> ignored) {
    throw new RuntimeException("Not implemented");
  }

  public AchievementsUser load(final String nickname) throws SQLException {
    final DatabaseReference<AchievementsUser> databaseReference = new DatabaseReference<>();
    this.doSelect("SELECT * FROM rhc_achievements_users WHERE nickname = ?",
        statement -> statement.setString(1, nickname),
        result -> databaseReference.set(new AchievementsUser(result)));
    return databaseReference.get();
  }

  private void setStatementParameters(final PreparedStatement statement,
      final AchievementsUser user) throws SQLException {
    statement.setString(1, user.getNickname());
    statement.setString(2, AchievementsUserSerializationHelper.serializeStatisticsMap(
        user.getAchievementStatisticMap()));
    statement.setString(3,
        AchievementsUserSerializationHelper.serializeBoostMap(user.getAchievementBoostMap()));
    statement.setString(4, AchievementsUserSerializationHelper.serializeCompletedAchievementSet(
        user.getCompletedAchievementSet()));
    statement.setString(5, user.getUniqueId().toString());
  }
}
