package pl.rosehc.waypoint;

import io.netty.buffer.Unpooled;
import java.awt.Color;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class WaypointHelper {

  private WaypointHelper() {
  }

  public static void createWaypoint(final Player player, final Location location,
      final int waypointId, final String waypointName, final Color color, final String assetSha,
      final long assetId, final long time) {
    final PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
    packetDataSerializer.writeByte(0);
    packetDataSerializer.b(waypointId);
    packetDataSerializer.a(waypointName);
    packetDataSerializer.a(
        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    packetDataSerializer.writeInt(color.getRGB());
    packetDataSerializer.writeByte(0);
    packetDataSerializer.writeBytes(asHexArray(assetSha));
    packetDataSerializer.b(assetId);
    packetDataSerializer.b(time);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
        new PacketPlayOutCustomPayload("bp:waypoint", packetDataSerializer));
  }

  public static void deleteWaypoint(final Player player, final int id) {
    final PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
    packetDataSerializer.writeByte(1);
    packetDataSerializer.b(id);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
        new PacketPlayOutCustomPayload("bp:waypoint", packetDataSerializer));
  }

  public static void deleteAllWaypoints(final Player player) {
    final PacketDataSerializer packetDataSerializer = new PacketDataSerializer(Unpooled.buffer());
    packetDataSerializer.writeByte(2);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
        new PacketPlayOutCustomPayload("bp:waypoint", packetDataSerializer));
  }

  private static byte[] asHexArray(final String string) {
    final char[] charArray = string.toCharArray();
    final byte[] byteArray = new byte[charArray.length / 2];

    for (int i = 0; i < charArray.length; i += 2) {
      byteArray[i / 2] = (byte) ((Character.digit(charArray[i], 16) << 4) + Character.digit(
          charArray[i + 1], 16));
    }

    return byteArray;
  }
}
