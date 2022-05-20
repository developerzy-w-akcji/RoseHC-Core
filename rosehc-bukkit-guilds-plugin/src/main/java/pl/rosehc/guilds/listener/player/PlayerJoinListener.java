package pl.rosehc.guilds.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.tablist.TabList;

public final class PlayerJoinListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerJoinListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final TabList tabList = this.plugin.getTabListFactory().findOrCreateTabList(player);
    tabList.update();
  }
}
