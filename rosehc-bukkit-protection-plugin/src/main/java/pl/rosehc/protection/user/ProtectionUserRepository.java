package pl.rosehc.protection.user;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.NotImplementedException;
import pl.rosehc.adapter.database.DatabaseAdapter;
import pl.rosehc.adapter.database.DatabaseReference;
import pl.rosehc.adapter.database.DatabaseRepository;
import pl.rosehc.protection.ProtectionPlugin;

public final class ProtectionUserRepository extends DatabaseRepository<UUID, ProtectionUser> {

  private final ProtectionPlugin plugin;

  public ProtectionUserRepository(final DatabaseAdapter databaseAdapter,
      final ProtectionPlugin plugin) throws SQLException {
    super(databaseAdapter);
    this.plugin = plugin;
  }

  @Override
  public Map<UUID, ProtectionUser> loadAll() {
    throw new NotImplementedException();
  }

  @Override
  public ProtectionUser load(final UUID uniqueId) throws SQLException {
    final DatabaseReference<ProtectionUser> databaseReference = new DatabaseReference<>();
    this.doSelect("SELECT * FROM rhc_protection_users WHERE uniqueId = ?",
        statement -> statement.setString(1, uniqueId.toString()),
        result -> databaseReference.set(new ProtectionUser(result)));
    return databaseReference.getOrDefault(new ProtectionUser(uniqueId,
            System.currentTimeMillis() + this.plugin.getProtectionConfiguration().parsedExpiryTime),
        this::insert);
  }

  @Override
  public void prepareTable() throws SQLException {
    this.consumeConnection(connection -> {
      try (final Statement statement = connection.createStatement()) {
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS rhc_protection_users (uniqueId CHAR(36) NOT NULL PRIMARY KEY, expiry_time BIGINT);");
      }
    });
  }

  @Override
  public void insert(final ProtectionUser user) throws SQLException {
    this.doUpdate("INSERT INTO rhc_protection_users (uniqueId, expiry_time) VALUES (?, ?)",
        statement -> {
          statement.setString(1, user.getUniqueId().toString());
          statement.setLong(2, user.getExpiryTime());
        });
  }

  @Override
  public void update(final ProtectionUser user) throws SQLException {
    this.doUpdate("UPDATE rhc_protection_users SET expiry_time = ? WHERE uniqueId = ?",
        statement -> {
          statement.setLong(1, user.getExpiryTime());
          statement.setString(2, user.getUniqueId().toString());
        });
  }

  @Override
  public void updateAll(final Collection<ProtectionUser> ignored) {
    throw new NotImplementedException();
  }

  @Override
  public void delete(final ProtectionUser ignored) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteAll(final Collection<ProtectionUser> ignored) {
    throw new NotImplementedException();
  }
}
