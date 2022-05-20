package pl.rosehc.guilds.guild;

import java.util.concurrent.ThreadLocalRandom;

public final class GuildPlayerHelpInfo {

  private final String nickname;
  private final long time;
  private final int waypointId;
  private int x, y, z;

  public GuildPlayerHelpInfo(final String nickname, final long time, final int x, final int y,
      final int z) {
    this.nickname = nickname;
    this.time = time;
    this.waypointId = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public String getNickname() {
    return this.nickname;
  }

  public int getWaypointId() {
    return this.waypointId;
  }

  public boolean isNotActive() {
    return this.time <= System.currentTimeMillis();
  }

  public int getX() {
    return this.x;
  }

  public void setX(final int x) {
    this.x = x;
  }

  public int getY() {
    return this.y;
  }

  public void setY(final int y) {
    this.y = y;
  }

  public int getZ() {
    return this.z;
  }

  public void setZ(final int z) {
    this.z = z;
  }
}
