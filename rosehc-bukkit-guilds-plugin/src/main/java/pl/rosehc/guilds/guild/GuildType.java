package pl.rosehc.guilds.guild;

public enum GuildType {

  SMALL(15, 0, 50),
  MEDIUM(35, 25, 40),
  LARGE(250, 50, 0);

  private final int size, damagePercentage, maxBlockPlaceY;

  GuildType(final int size, final int damagePercentage, final int maxBlockPlaceY) {
    this.size = size;
    this.damagePercentage = damagePercentage;
    this.maxBlockPlaceY = maxBlockPlaceY;
  }

  public int getSize() {
    return this.size;
  }

  public int getDamagePercentage() {
    return this.damagePercentage;
  }

  public int getMaxBlockPlaceY() {
    return this.maxBlockPlaceY;
  }
}
