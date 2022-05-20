package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildJoinAlertMessageUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildSetJoinAlertMessageCommand {

  private final GuildsPlugin plugin;

  public GuildSetJoinAlertMessageCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild setjoinalert", "g setjoinalert", "guild powiadomienie",
      "g powiadomienie"}, description = "Ustawia alert po wejściu na serwer.")
  public void handleGuildSetJoinAlertMessage(final @Sender Player player,
      final @Name("text (jeżeli chcesz usunąć powiadomienie, wpisz tutaj none)") @Combined String text) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildSetJoinAlertYouDontHaveAnyGuild));
    }

    final GuildMember member = guild.getGuildMember(user);
    if (member == null || !member.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildSetJoinAlertYouCantSetAlert));
    }

    ChatHelper.sendMessage(player, !text.equalsIgnoreCase("none")
        ? this.plugin.getGuildsConfiguration().messagesWrapper.guildSetJoinAlertSetSuccess.replace(
        "{ALERT_MESSAGE}", text)
        : this.plugin.getGuildsConfiguration().messagesWrapper.guildSetJoinAlertRemoveSuccess);
    guild.setJoinAlertMessage(!text.equalsIgnoreCase("none") ? text : null);
    this.plugin.getRedisAdapter().sendPacket(
        new GuildJoinAlertMessageUpdatePacket(guild.getTag(), guild.getJoinAlertMessage()),
        "rhc_master_controller", "rhc_platform");
  }
}
