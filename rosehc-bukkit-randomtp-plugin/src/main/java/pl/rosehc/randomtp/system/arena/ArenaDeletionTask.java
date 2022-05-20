package pl.rosehc.randomtp.system.arena;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class ArenaDeletionTask implements Runnable {

  private final List<Location> locationsToResetList;
  private BukkitTask task;

  public ArenaDeletionTask(final List<Location> locationsToResetList) {
    this.locationsToResetList = locationsToResetList;
  }

  @Override
  public void run() {
    for (int i = 0; i < 5; i++) {
      if (this.locationsToResetList.isEmpty()) {
        this.task.cancel();
        break;
      }

      final Location location = this.locationsToResetList.remove(0);
      location.getBlock().setType(Material.AIR);
    }
  }

  public void start() {
    this.task = Bukkit.getScheduler()
        .runTaskTimer(SystemRandomTPPlugin.getInstance().getOriginal(), this, 1L, 1L);
  }
}
