package pl.rosehc.guilds.guild;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import pl.rosehc.controller.wrapper.guild.GuildRegenerationBlockStateSerializationWrapper;

public final class GuildRegenerationBlockState {

  private final Material material;
  private final byte data;
  private final int x, y, z;
  private boolean wasAdded;

  public GuildRegenerationBlockState(final Material material, final byte data, final int x,
      final int y, final int z) {
    this.material = material;
    this.data = data;
    this.x = x;
    this.y = y;
    this.z = z;
    this.wasAdded = true;
  }

  public static GuildRegenerationBlockState create(
      final GuildRegenerationBlockStateSerializationWrapper wrapper) {
    return new GuildRegenerationBlockState(Material.matchMaterial(wrapper.getMaterial()),
        wrapper.getData(), wrapper.getX(), wrapper.getY(), wrapper.getZ());
  }

  public GuildRegenerationBlockStateSerializationWrapper wrap() {
    return new GuildRegenerationBlockStateSerializationWrapper(this.material.name(), this.data,
        this.x, this.y, this.z);
  }

  public boolean wasAdded() {
    final boolean wasAdded = this.wasAdded;
    if (wasAdded) {
      this.wasAdded = false;
    }

    return wasAdded;
  }

  public void update(final World world) {
    final Block block = world.getBlockAt(this.x, this.y, this.z);
    block.setType(this.material);
    block.setData(this.data);
  }
}
