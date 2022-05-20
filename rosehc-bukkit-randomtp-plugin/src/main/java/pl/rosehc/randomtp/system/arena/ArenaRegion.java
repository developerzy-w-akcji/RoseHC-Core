package pl.rosehc.randomtp.system.arena;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class ArenaRegion {

  private final Location centerLocation;
  private final Vector minimumPoint, maximumPoint;
  private final WorldBorder border;

  ArenaRegion(final Location centerLocation) {
    final int cuboidSize =
        SystemRandomTPPlugin.getInstance().getRandomTPConfiguration().cuboidSize / 2;
    this.centerLocation = centerLocation;
    this.minimumPoint = new Vector(centerLocation.getX() - cuboidSize, 0D,
        centerLocation.getZ() - cuboidSize);
    this.maximumPoint = new Vector(centerLocation.getX() + cuboidSize, 256D,
        centerLocation.getZ() + cuboidSize);
    this.border = new WorldBorder();
    this.border.setCenter(centerLocation.getX(), centerLocation.getZ());
    this.border.setSize((cuboidSize * 2D) + 2D);
  }

  public Location getCenterLocation() {
    return this.centerLocation;
  }

  public boolean isInside(final Location location) {
    return location.toVector().isInAABB(this.minimumPoint, this.maximumPoint);
  }

  public void sendBorder(final Player player) {
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
        new PacketPlayOutWorldBorder(this.border, EnumWorldBorderAction.INITIALIZE));
  }
}
