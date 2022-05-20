package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildMemberRemovePacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildHelper;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildLeaveCommand {

  private final GuildsPlugin plugin;

  public GuildLeaveCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild leave", "g leave", "guild opusc",
      "g opusc"}, description = "Opuszcza podaną gildii.")
  public void handleGuildLeave(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveYouDontHaveAnyGuild));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveBadErrorOccurred));
    }

    if (guildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveYouCannotLeaveAsLeader));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveSuccessSender.replace(
            "{TAG}", guild.getTag()).replace("{NAME}", guild.getName()));
    guild.removeGuildMember(user);
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveSuccessGuild.replace(
            "{PLAYER_NAME}", player.getName()));
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildMemberRemovePacket(guild.getTag(), player.getUniqueId()),
            "rhc_master_controller", "rhc_guilds");
    this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaveSuccessBroadcast.replace(
                "{PLAYER_NAME}", player.getName()).replace("{TAG}", guild.getTag())
            .replace("{NAME}", guild.getName()), false), "rhc_platform");
    GuildHelper.teleportOutFromTerrain(player, guild);
  }
}
