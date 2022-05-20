package pl.rosehc.guilds.guild.scanner;

public final class GuildBlockScannerData {

  private final int x, y, z;

  public GuildBlockScannerData(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getZ() {
    return this.z;
  }
}
