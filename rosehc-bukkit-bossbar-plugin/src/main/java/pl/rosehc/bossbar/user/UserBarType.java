package pl.rosehc.bossbar.user;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author stevimeister on 05.02.2021
 **/
public enum UserBarType {

  NOTIFICATION(UserBarConstants.NOTIFICATION_UUID),
  ITEM_SHOP(UserBarConstants.ITEM_SHOP_UUID),
  GUILD(UserBarConstants.GUILD_UUID),
  SECTOR(UserBarConstants.SECTOR_UUID),
  PROTECTION(UserBarConstants.PROTECTION_UUID),
  SPECIAL_BAR(UserBarConstants.SPECIAL_BAR_UUID);

  private final UUID uniqueId;

  UserBarType(final UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  public static Optional<UserBarType> findBossBarByUniqueId(final UUID uniqueId) {
    return Arrays.stream(values())
        .filter(userBar -> Objects.equals(userBar.getUniqueId(), uniqueId))
        .findFirst();
  }

  public UUID getUniqueId() {
    return uniqueId;
  }
}

