package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildMemberUpdateRankPacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildLeaderCommand {

  private final GuildsPlugin plugin;

  public GuildLeaderCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild leader", "g leader", "guild lider",
      "g lider"}, description = "Oddaje lidera podanemu graczu.")
  public void handleGuildLeader(final @Sender Player player,
      final @Name("nickname") GuildUser targetUser) {
    final GuildUser playerUser = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = playerUser.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderYouDontHaveAnyGuild));
    }

    final GuildMember playerGuildMember = guild.getGuildMember(playerUser);
    if (playerGuildMember == null || !playerGuildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderYouDontHavePermission));
    }

    if (targetUser.getGuild() == null || !targetUser.getGuild().equals(guild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderTargetIsNotInYourGuild));
    }

    final GuildMember targetGuildMember = guild.getGuildMember(targetUser);
    if (targetGuildMember == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderTargetIsNotInYourGuild));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderSuccessSender.replace(
            "{PLAYER_NAME}", targetUser.getNickname()));
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildLeaderSuccessGuild.replace(
                "{FIRST_PLAYER_NAME}", player.getName())
            .replace("{SECOND_PLAYER_NAME}", targetUser.getNickname()));
    playerGuildMember.setGroup(guild.getDefaultGroup());
    targetGuildMember.setGroup(guild.getLeaderGroup());
    this.plugin.getRedisAdapter().sendPacket(
        new GuildMemberUpdateRankPacket(guild.getTag(), player.getUniqueId(),
            guild.getLeaderGroup().getUniqueId()), "rhc_master_controller", "rhc_guilds");
  }
}
