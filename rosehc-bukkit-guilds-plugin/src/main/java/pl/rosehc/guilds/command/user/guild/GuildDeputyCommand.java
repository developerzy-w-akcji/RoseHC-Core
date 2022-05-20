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

public final class GuildDeputyCommand {

  private final GuildsPlugin plugin;

  public GuildDeputyCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild deputy", "g deputy", "guild zastepca",
      "g zastepca"}, description = "Nadaje zastępce podanemu graczu.")
  public void handleGuildDeputy(final @Sender Player player,
      final @Name("nickname") GuildUser targetUser) {
    final GuildUser playerUser = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = playerUser.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyYouDontHaveAnyGuild));
    }

    final GuildMember playerGuildMember = guild.getGuildMember(playerUser);
    if (playerGuildMember == null || !playerGuildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyYouDontHavePermission));
    }

    if (targetUser.getGuild() == null || !targetUser.getGuild().equals(guild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyTargetIsNotInYourGuild));
    }

    final GuildMember targetGuildMember = guild.getGuildMember(targetUser);
    if (targetGuildMember == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyTargetIsNotInYourGuild));
    }

    if (targetGuildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyYouCannotGiveDeputyToLeader));
    }

    if (targetGuildMember.isDeputy()) {
      ChatHelper.sendMessage(player,
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyRemoveSuccessSender.replace(
              "{PLAYER_NAME}", player.getName()));
      targetGuildMember.setGroup(guild.getDefaultGroup());
      guild.broadcastChatMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyRemoveSuccessGuild.replace(
                  "{FIRST_PLAYER_NAME}", player.getName())
              .replace("{SECOND_PLAYER_NAME}", targetUser.getNickname()));
      this.plugin.getRedisAdapter().sendPacket(
          new GuildMemberUpdateRankPacket(guild.getTag(), targetGuildMember.getUniqueId(),
              guild.getDefaultGroup().getUniqueId()), "rhc_master_controller", "rhc_platform");
      return;
    }

    int deputiesInGuild = 0;
    for (final GuildMember member : guild.getGuildMembers()) {
      if (member != null && member.isDeputy()) {
        deputiesInGuild++;
      }
    }

    if (deputiesInGuild >= this.plugin.getGuildsConfiguration().pluginWrapper.guildDeputyLimit) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildHasTooManyDeputies));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyAddSuccessSender.replace(
            "{PLAYER_NAME}", player.getName()));
    targetGuildMember.setGroup(guild.getDeputyGroup());
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildDeputyAddSuccessGuild.replace(
                "{FIRST_PLAYER_NAME}", player.getName())
            .replace("{SECOND_PLAYER_NAME}", targetUser.getNickname()));
    this.plugin.getRedisAdapter().sendPacket(
        new GuildMemberUpdateRankPacket(guild.getTag(), targetGuildMember.getUniqueId(),
            guild.getDeputyGroup().getUniqueId()), "rhc_master_controller", "rhc_platform");
  }
}
