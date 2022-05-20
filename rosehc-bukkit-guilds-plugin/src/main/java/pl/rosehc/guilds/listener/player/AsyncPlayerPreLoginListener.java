package pl.rosehc.guilds.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pl.rosehc.controller.packet.guild.user.GuildUserCreatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.user.GuildUser;

public final class AsyncPlayerPreLoginListener implements Listener {

  private final GuildsPlugin plugin;

  public AsyncPlayerPreLoginListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLogin(final AsyncPlayerPreLoginEvent event) {
    if (!this.plugin.getGuildUserFactory().findUserByUniqueId(event.getUniqueId()).isPresent()) {
      final GuildUser user = new GuildUser(event.getUniqueId(), event.getName());
      this.plugin.getGuildUserFactory().addUser(user);
      this.plugin.getRedisAdapter()
          .sendPacket(new GuildUserCreatePacket(user.getUniqueId(), user.getNickname()),
              "rhc_master_controller", "rhc_guilds");
    }
  }
}
