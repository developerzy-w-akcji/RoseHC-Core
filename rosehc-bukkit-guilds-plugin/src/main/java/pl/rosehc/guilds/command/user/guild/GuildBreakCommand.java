package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildUpdateAllyPacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildBreakCommand {

  private final GuildsPlugin plugin;

  public GuildBreakCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild break", "g break", "guild zerwij",
      "g zerwij"}, description = "Zrywa sojusz z daną gildią.")
  public void handleGuildAlly(final @Sender Player player, final @Name("tag") Guild targetGuild) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild userGuild = user.getGuild();
    if (userGuild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakYouDontHaveAnyGuild));
    }

    if (targetGuild.equals(userGuild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakYouCannotBreakAllyWithYourOwnGuild));
    }

    final GuildMember guildMember = userGuild.getGuildMember(user);
    if (guildMember == null || !guildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakYouCannotBreakAllyWithOtherGuild));
    }

    if (userGuild.getAlliedGuild() == null || !userGuild.getAlliedGuild().equals(targetGuild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakYouDontHaveAllyWithThisGuild));
    }

    userGuild.broadcastChatMessage(ChatHelper.colored(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakSuccessGuildSender.replace(
            "{TAG}", targetGuild.getTag())).replace("{NAME}", targetGuild.getName()));
    targetGuild.broadcastChatMessage(ChatHelper.colored(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakSuccessGuildReceiver.replace(
            "{TAG}", userGuild.getTag())).replace("{NAME}", userGuild.getName()));
    targetGuild.setAlliedGuild(null);
    userGuild.setAlliedGuild(null);
    this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildBreakSuccessBroadcast.replace(
                "{FIRST_TAG}", userGuild.getTag()).replace("{FIRST_NAME}", userGuild.getName())
            .replace("{SECOND_TAG}", targetGuild.getTag())
            .replace("{SECOND_NAME}", targetGuild.getName()), false));
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildUpdateAllyPacket(targetGuild.getTag(), userGuild.getTag(), false),
            "rhc_master_controller", "rhc_platform");
  }
}
