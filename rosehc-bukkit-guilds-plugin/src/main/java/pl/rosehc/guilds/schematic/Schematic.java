package pl.rosehc.guilds.schematic;

public final class Schematic {

  private final short[] blocks;
  private final byte[] data;
  private final short width, length, height;

  public Schematic(final short[] blocks, final byte[] data, final short width, final short length,
      final short height) {
    this.blocks = blocks;
    this.data = data;
    this.width = width;
    this.length = length;
    this.height = height;
  }

  public short[] getBlocks() {
    return this.blocks;
  }

  public byte[] getData() {
    return this.data;
  }

  public short getWidth() {
    return this.width;
  }

  public short getLength() {
    return this.length;
  }

  public short getHeight() {
    return this.height;
  }
}
