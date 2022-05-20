package pl.rosehc.guilds.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.rosehc.adapter.nametag.NameTagPlayerEvent;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class PlayerNameTagListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerNameTagListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onNameTag(final NameTagPlayerEvent event) {
    this.plugin.getGuildUserFactory().findUserByUniqueId(event.getPlayer().getUniqueId()).ifPresent(
        playerUser -> this.plugin.getGuildUserFactory()
            .findUserByUniqueId(event.getRequester().getUniqueId()).ifPresent(viewerUser -> {
              final Guild playerGuild = playerUser.getGuild(), viewerGuild = viewerUser.getGuild();
              if (playerGuild == null) {
                return;
              }

              final String tagRelation = this.getTagRelation(playerGuild, viewerGuild);
              event.setPrefix(
                  event.getPrefix() + tagRelation.replace("{TAG}", playerGuild.getTag()) + " ");
            }));
  }

  private String getTagRelation(final Guild playerGuild, final Guild viewerGuild) {
    if (viewerGuild != null) {
      return playerGuild.equals(viewerGuild)
          ? this.plugin.getGuildsConfiguration().messagesWrapper.nameTagYourGuildPrefix
          : playerGuild.getAlliedGuild() != null && playerGuild.getAlliedGuild().equals(viewerGuild)
              ? this.plugin.getGuildsConfiguration().messagesWrapper.nameTagAllyGuildPrefix
              : this.plugin.getGuildsConfiguration().messagesWrapper.nameTagEnemyGuildPrefix;
    }

    return this.plugin.getGuildsConfiguration().messagesWrapper.nameTagEnemyGuildPrefix;
  }
}
