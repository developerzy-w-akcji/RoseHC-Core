package pl.rosehc.guilds.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoRemovePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class PlayerQuitListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerQuitListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    this.plugin.getTabListFactory().removeTabList(player);
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
      user.setEnteredGuild(null);
      user.setPreparedForGuildDeletion(false);
      final Guild guild = user.getGuild();
      if (guild != null && !SectorsPlugin.getInstance().getSectorUserFactory()
          .findUserByUniqueId(player.getUniqueId()).filter(SectorUser::isRedirecting).isPresent()) {
        guild.findGuildPlayerHelpInfo(player.getUniqueId()).ifPresent(helpInfo -> {
          guild.removeGuildPlayerHelpInfo(player.getUniqueId());
          this.plugin.getRedisAdapter().sendPacket(
              new GuildHelpInfoRemovePacket(guild.getTag(), player.getUniqueId(), false),
              "rhc_master_controller", "rhc_guilds");
        });
        guild.findGuildAllyPlayerHelpInfo(player.getUniqueId()).ifPresent(helpInfo -> {
          guild.removeGuildAllyPlayerHelpInfo(player.getUniqueId());
          this.plugin.getRedisAdapter()
              .sendPacket(new GuildHelpInfoRemovePacket(guild.getTag(), player.getUniqueId(), true),
                  "rhc_master_controller", "rhc_guilds");
        });
      }
    });
  }
}
