package pl.rosehc.protection.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class ProtectionUser {

  private final UUID uniqueId;
  private long expiryTime;
  private boolean expiryTimeChanged;

  public ProtectionUser(final UUID uniqueId, final long expiryTime) {
    this.uniqueId = uniqueId;
    this.expiryTime = expiryTime;
  }

  public ProtectionUser(final ResultSet result) throws SQLException {
    this(UUID.fromString(result.getString("uniqueId")), result.getLong("expiry_time"));
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public long getExpiryTime() {
    return this.expiryTime;
  }

  public void setExpiryTime(final long expiryTime) {
    this.expiryTime = expiryTime;
    if (expiryTime != 0L) {
      this.expiryTimeChanged = true;
    }
  }

  public boolean isExpiryTimeChanged() {
    final boolean expiryTimeChanged = this.expiryTimeChanged;
    this.expiryTimeChanged = false;
    return expiryTimeChanged;
  }

  public boolean hasExpired() {
    return this.expiryTime <= System.currentTimeMillis();
  }
}
