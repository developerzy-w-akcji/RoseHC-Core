package pl.rosehc.bossbar;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;

public final class BossBarPacket {

  private BarOperation operation;
  private UUID uuid;
  private String title;
  private float progress;
  private BarColor color;
  private BarStyle style;

  BossBarPacket() {
  }

  UUID getUuid() {
    return uuid;
  }

  BossBarPacket setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public BarOperation getOperation() {
    return operation;
  }

  void setOperation(BarOperation operation) {
    this.operation = operation;
  }


  String getTitle() {
    return this.title;
  }

  BossBarPacket setTitle(String title) {
    this.title = title;
    return this;
  }

  public float getProgress() {
    return progress;
  }

  BossBarPacket setProgress(float progress) {
    this.progress = progress;
    return this;
  }

  BarColor getColor() {
    return color;
  }

  BossBarPacket setColor(BarColor color) {
    this.color = color;
    return this;
  }

  BarStyle getStyle() {
    return style;
  }

  BossBarPacket setStyle(BarStyle style) {
    this.style = style;
    return this;
  }

  /* serialize (payload name: BP|UpdateBossInfo) */

  public PacketDataSerializer serialize() {
    final PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
    packetDataSerializer.a(uuid); // writeUUID
    packetDataSerializer.a(operation); // writeEnum - varint

    switch (this.operation) {
      case ADD:
        packetDataSerializer.a(this.title); // writeVarString
        packetDataSerializer.writeFloat(this.progress);
        packetDataSerializer.a(this.color); // writeEnum - varint
        packetDataSerializer.a(this.style); // writeEnum - varint
        packetDataSerializer.writeByte(0);
        break;
      case REMOVE:
      default:
        break;
      case UPDATE_PCT:
        packetDataSerializer.writeFloat(this.progress);
        break;

      case UPDATE_NAME:
        packetDataSerializer.a(this.title); // writeString
        break;

      case UPDATE_STYLE:
        packetDataSerializer.a(this.color); // writeEnum - varint
        packetDataSerializer.a(this.style); // writeEnum - varint
        break;

      case UPDATE_PROPERTIES:
        packetDataSerializer.writeByte(0);
    }

    packetDataSerializer.capacity(packetDataSerializer.readableBytes());
    return packetDataSerializer;
  }

}