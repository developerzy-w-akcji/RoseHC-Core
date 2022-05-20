package pl.rosehc.guilds.command.user.guild;

import java.util.HashSet;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildMemberAddPacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildJoinCommand {

  private final GuildsPlugin plugin;

  public GuildJoinCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild join", "g join", "guild dolacz",
      "g dolacz"}, description = "Dołącza do podanej gildii.")
  public void handleGuildJoin(final @Sender Player player, final @Name("tag") Guild guild) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    if (user.getGuild() != null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinYouAlreadyHaveGuild));
    }

    if (!guild.isMemberInvited(user)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinYouDontHaveInviteFromThisGuild));
    }

    final GuildMember member = new GuildMember(user.getUniqueId(), user, new HashSet<>(),
        guild.getDefaultGroup());
    if (!guild.addGuildMember(member)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinNoFreeSlotWasFound));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinSuccessSender.replace("{TAG}",
            guild.getTag()).replace("{NAME}", guild.getName()));
    guild.removeMemberInvite(user);
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinSuccessGuild.replace(
            "{PLAYER_NAME}", player.getName()));
    this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildJoinSuccessBroadcast.replace(
                "{PLAYER_NAME}", player.getName()).replace("{TAG}", guild.getTag())
            .replace("{NAME}", guild.getName()), false), "rhc_platform");
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildMemberAddPacket(guild.getTag(), member.wrap()),
            "rhc_master_controller", "rhc_guilds");
  }
}
