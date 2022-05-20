package pl.rosehc.guilds.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class AsyncPlayerChatListener implements Listener {

  private final GuildsPlugin plugin;

  public AsyncPlayerChatListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onChat(final AsyncPlayerChatEvent event) {
    final Player player = event.getPlayer();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
      final Guild guild = user.getGuild();
      final String message = event.getMessage();
      if (guild != null && (message.startsWith("!!") || message.startsWith("!"))) {
        event.setCancelled(true);
        if (message.startsWith("!!")) {
          final String allyGuildChatMessageFormat = this.plugin.getGuildsConfiguration().messagesWrapper.guildChatFormatAlly.replace(
                  "{PLAYER_NAME}", player.getName())
              .replace("{MESSAGE}", event.getMessage().substring(2));
          guild.broadcastChatMessage(allyGuildChatMessageFormat);
          if (guild.getAlliedGuild() != null) {
            guild.getAlliedGuild().broadcastChatMessage(allyGuildChatMessageFormat);
          }
          return;
        }

        guild.broadcastChatMessage(
            this.plugin.getGuildsConfiguration().messagesWrapper.guildChatFormatGuild.replace(
                    "{PLAYER_NAME}", player.getName())
                .replace("{MESSAGE}", event.getMessage().substring(1)));
        return;
      }

      event.setFormat(event.getFormat()
          .replace("{POINTS}", String.valueOf(user.getUserRanking().getPoints()))
          .replace("{TAG}", guild != null ? guild.getTag() + " " : ""));
    });
  }
}
