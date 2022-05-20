package pl.rosehc.guilds.guild.scanner;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class GuildBlockScanner {

  private static final int MAX_BLOCKS_PER_SCAN = 450, MAX_BLOCKS_Y = 65;
  private final Set<Material> materialsToScanSet;
  private final Map<Material, AtomicInteger> materialCountMap;
  private final Consumer<Map<Material, AtomicInteger>> postScanAction;
  private final Guild guild;
  private final BukkitTask scanTask;
  private final AtomicInteger processedBlocks;

  private LinkedList<GuildBlockScannerData> blockScannerDataList;
  private boolean preparing;
  private int maxBlocksToScan;

  public GuildBlockScanner(final Set<Material> materialsToScanSet,
      final Consumer<Map<Material, AtomicInteger>> postScanAction, final Guild guild) {
    this.materialsToScanSet = materialsToScanSet;
    this.materialCountMap = new ConcurrentHashMap<>();
    this.postScanAction = postScanAction;
    this.guild = guild;
    this.processedBlocks = new AtomicInteger();
    this.scanTask = Bukkit.getScheduler()
        .runTaskTimer(GuildsPlugin.getInstance(), this::scan, 5L, 1L);
    this.preparing = true;
  }

  public Set<Material> getMaterialsToScanSet() {
    return this.materialsToScanSet;
  }

  public int getProcessedBlocks() {
    return this.processedBlocks.get();
  }

  public int getMaxBlocksToScan() {
    return this.maxBlocksToScan;
  }

  public boolean isPreparing() {
    return this.preparing;
  }

  private void scan() {
    if (this.blockScannerDataList == null) {
      final Vector minimumPoint = this.guild.getGuildRegion()
          .getMinimumPoint(), maximumPoint = this.guild.getGuildRegion().getMaximumPoint();
      this.blockScannerDataList = new LinkedList<>();
      for (int x = minimumPoint.getBlockX(), maxX = maximumPoint.getBlockX(); x <= maxX; x++) {
        for (int y = 0; y <= MAX_BLOCKS_Y; y++) {
          for (int z = minimumPoint.getBlockZ(), maxZ = maximumPoint.getBlockZ(); z <= maxZ; z++) {
            this.blockScannerDataList.offerFirst(new GuildBlockScannerData(x, y, z));
          }
        }
      }

      this.maxBlocksToScan = this.blockScannerDataList.size();
      this.preparing = false;
    }

    if (this.blockScannerDataList.isEmpty()) {
      this.scanTask.cancel();
      this.postScanAction.accept(this.materialCountMap);
      return;
    }

    final World world = Bukkit.getWorlds().get(0);
    int currentTickBlocks = 0;
    while (currentTickBlocks < MAX_BLOCKS_PER_SCAN) {
      final GuildBlockScannerData blockData = this.blockScannerDataList.pollFirst();
      if (blockData == null) {
        break;
      }

      final Block block = world.getBlockAt(blockData.getX(), blockData.getY(), blockData.getZ());
      final Material blockType = block.getType();
      if (this.materialsToScanSet.contains(blockType)) {
        final AtomicInteger count = this.materialCountMap.computeIfAbsent(blockType,
            ignored -> new AtomicInteger());
        count.incrementAndGet();
      }

      currentTickBlocks++;
      this.processedBlocks.incrementAndGet();
    }
  }

  public void cancel() {
    this.scanTask.cancel();
  }
}
