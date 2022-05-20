package pl.rosehc.guilds.tablist;

import org.bukkit.entity.Player;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.sectors.SectorsPlugin;

public final class TabListUpdateTask implements Runnable {

  private final GuildsPlugin plugin;

  public TabListUpdateTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 20L, 20L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      if (SectorsPlugin.getInstance().getSectorUserFactory()
          .findUserByUniqueId(player.getUniqueId()).filter(user -> !user.isRedirecting())
          .isPresent()) {
        final TabList tabList = this.plugin.getTabListFactory().findOrCreateTabList(player);
        tabList.update();
      }
    }
  }
}
