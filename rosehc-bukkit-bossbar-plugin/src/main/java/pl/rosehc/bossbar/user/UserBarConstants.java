package pl.rosehc.bossbar.user;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UserBarConstants {

  public static final UUID GUILD_UUID = generate("guild");
  public static final UUID ITEM_SHOP_UUID = generate("item_shop");
  public static final UUID NOTIFICATION_UUID = generate("notification");
  public static final UUID SECTOR_UUID = generate("sector");
  public static final UUID PROTECTION_UUID = generate("protection");
  public static final UUID SPECIAL_BAR_UUID = generate("special_bar");

  private static UUID generate(final String owner) {
    return UUID.nameUUIDFromBytes(("rosehc-bar:" + owner).getBytes(StandardCharsets.UTF_8));
  }
}
