package pl.rosehc.guilds.listener.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.platform.vanishingblock.antigrief.AntiGriefBlockPlaceEvent;

public final class AntiGriefBlockPlaceListener implements Listener {

  private final GuildsPlugin plugin;

  public AntiGriefBlockPlaceListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onAntiGriefBlockPlace(final AntiGriefBlockPlaceEvent event) {
    this.plugin.getGuildUserFactory().findUserByUniqueId(event.getPlayer().getUniqueId())
        .ifPresent(user -> {
          final Guild guild = user.getGuild();
          if (guild != null && guild.getGuildMember(user) != null) {
            event.setCancelled(true);
          }
        });
  }
}
