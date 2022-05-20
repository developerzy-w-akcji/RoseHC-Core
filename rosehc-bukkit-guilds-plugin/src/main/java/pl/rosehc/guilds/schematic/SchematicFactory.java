package pl.rosehc.guilds.schematic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class SchematicFactory {

  private volatile Schematic guildSchematic;

  public synchronized void pasteGuildSchematic(final Location targetLocation) {
    final Schematic guildSchematic = this.guildSchematic;
    if (guildSchematic == null) {
      return;
    }

    for (int x = 0; x < guildSchematic.getWidth(); ++x) {
      for (int y = 0; y < guildSchematic.getHeight(); ++y) {
        for (int z = 0; z < guildSchematic.getLength(); ++z) {
          final Material material = Material.getMaterial(guildSchematic.getBlocks()[
              (y * guildSchematic.getWidth() * guildSchematic.getLength()) + (z
                  * guildSchematic.getWidth()) + x] & 0xFF);
          if (!Objects.isNull(material)) {
            final Block block = targetLocation.getWorld()
                .getBlockAt((targetLocation.getBlockX() - (guildSchematic.getWidth() / 2)) + x,
                    (targetLocation.getBlockY() - (guildSchematic.getHeight() / 2)) + y,
                    (targetLocation.getBlockZ() - (guildSchematic.getLength() / 2)) + z);
            block.setType(material, true);
          }
        }
      }
    }
  }

  public synchronized void reloadGuildSchematic(final byte[] input) throws IOException {
    if (input == null) {
      return;
    }

    try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input)) {
      final NBTTagCompound compound = NBTCompressedStreamTools.a(byteArrayInputStream);
      final byte[] blocks = compound.getByteArray("Blocks"), data = compound.getByteArray(
          "Data"), addId =
          compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
      final short[] shortBlocks = new short[blocks.length];
      for (int index = 0; index < blocks.length; index++) {
        shortBlocks[index] = ((index >> 1) >= addId.length) ? (short) (blocks[index] & 0xFF)
            : (index & 1) == 0 ? (short) (((addId[index >> 1] & 0x0F) << 8) + (blocks[index]
                & 0xFF)) : (short) (((addId[index >> 1] & 0xF0) << 4) + (blocks[index] & 0xFF));
      }

      final short width = compound.getShort("Width"), height = compound.getShort(
          "Height"), length = compound.getShort("Length");
      this.guildSchematic = new Schematic(shortBlocks, data, width, length, height);
    }
  }
}
