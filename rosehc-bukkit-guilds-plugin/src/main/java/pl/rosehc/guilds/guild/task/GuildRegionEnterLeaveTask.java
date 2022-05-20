package pl.rosehc.guilds.guild.task;

import java.util.Optional;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsConfiguration.MessagesWrapper.TitleMessageWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class GuildRegionEnterLeaveTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildRegionEnterLeaveTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 10L, 10L);
  }

  @Override
  public void run() {
    final TitleMessageWrapper guildEnterTitle = this.plugin.getGuildsConfiguration().messagesWrapper.guildEnterTitle;
    final TitleMessageWrapper guildLeaveTitle = this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveTitle;
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
        final Optional<Guild> insideGuildOptional = this.plugin.getGuildFactory()
            .findGuildInside(player.getLocation());
        final Guild enteredGuild = user.getEnteredGuild();
        if (!insideGuildOptional.isPresent()) {
          if (enteredGuild != null) {
            ChatHelper.sendTitle(player,
                guildLeaveTitle.title.replace("{NAME}", enteredGuild.getName())
                    .replace("{TAG}", enteredGuild.getTag()),
                guildLeaveTitle.subTitle.replace("{NAME}", enteredGuild.getName())
                    .replace("{TAG}", enteredGuild.getTag()), guildLeaveTitle.fadeIn,
                guildLeaveTitle.stay, guildLeaveTitle.fadeOut);
            user.setEnteredGuild(null);
          }
          return;
        }

        final Guild insideGuild = insideGuildOptional.get();
        if (enteredGuild == null || !enteredGuild.equals(insideGuild)) {
          ChatHelper.sendTitle(player,
              guildEnterTitle.title.replace("{NAME}", insideGuild.getName())
                  .replace("{TAG}", insideGuild.getTag()),
              guildEnterTitle.subTitle.replace("{NAME}", insideGuild.getName())
                  .replace("{TAG}", insideGuild.getTag()), guildEnterTitle.fadeIn,
              guildEnterTitle.stay, guildEnterTitle.fadeOut);
          if (user.getGuild() == null || !user.getGuild().equals(insideGuild)) {
            insideGuild.broadcastChatMessage(
                this.plugin.getGuildsConfiguration().messagesWrapper.enemyHadJustEnteredYourGuildTerrain);
          }

          user.setEnteredGuild(insideGuild);
        }
      });
    }
  }
}
