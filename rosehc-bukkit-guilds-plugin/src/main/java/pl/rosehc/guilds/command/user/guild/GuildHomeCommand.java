package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.SectorType;

public final class GuildHomeCommand {

  private final GuildsPlugin plugin;

  public GuildHomeCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild home", "g home", "guild base", "g base", "guild dom", "guild baza",
      "g baza", "g dom"}, description = "Teleportuję gracza na dom gildii.")
  public void handleGuildHome(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildHomeNoGuildFound));
    }

    PlatformPlugin.getInstance().getTimerTaskFactory().addTimer(player, guild.getHomeLocation(),
        !SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getType()
            .equals(SectorType.SPAWN) ? 10 : 3);
  }
}
