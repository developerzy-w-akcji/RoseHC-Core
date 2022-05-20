package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildMemberInviteAddPacket;
import pl.rosehc.controller.packet.guild.guild.GuildMemberInviteRemovePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildInviteCommand {

  private final GuildsPlugin plugin;

  public GuildInviteCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild invite", "g invite", "guild zapros",
      "g zapros"}, description = "Zaprasza podanego gracza do gildii.")
  public void handleGuildInvite(final @Sender Player player,
      final @Name("nickname") GuildUser receiverUser) {
    final GuildUser senderUser = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = senderUser.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteNoGuildFound));
    }

    final GuildMember guildMember = guild.getGuildMember(senderUser);
    if (guildMember == null || !guildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteCannotInvitePlayer));
    }

    if (guild.isMemberInvited(receiverUser)) {
      ChatHelper.sendMessage(player,
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteCancelledSender.replace(
              "{PLAYER_NAME}", receiverUser.getNickname()));
      guild.removeMemberInvite(receiverUser);
      receiverUser.sendMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteCancelledReceiver.replace(
              "{PLAYER_NAME}", player.getName()).replace("{TAG}", guild.getTag()));
      this.plugin.getRedisAdapter()
          .sendPacket(new GuildMemberInviteRemovePacket(guild.getTag(), receiverUser.getUniqueId()),
              "rhc_master_controller", "rhc_guilds");
      return;
    }

    if (receiverUser.getGuild() != null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteThisUserAlreadyHaveGuild));
    }

    if (guild.isFull()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteCannotInviteWhenGuildIsFull));
    }

    final long inviteTime = System.currentTimeMillis()
        + this.plugin.getGuildsConfiguration().pluginWrapper.parsedGuildMemberInviteConsiderationTimeoutTime;
    guild.addMemberInvite(receiverUser, inviteTime);
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteSuccessSender.replace(
            "{PLAYER_NAME}", receiverUser.getNickname()));
    receiverUser.sendMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildInviteSuccessReceiver.replace(
                "{TAG}", guild.getTag()).replace("{NAME}", guild.getName())
            .replace("{PLAYER_NAME}", player.getName())
            .replace("{TIME}", TimeHelper.timeToString(inviteTime - System.currentTimeMillis())));
    this.plugin.getRedisAdapter().sendPacket(
        new GuildMemberInviteAddPacket(guild.getTag(), receiverUser.getUniqueId(), inviteTime),
        "rhc_master_controller", "rhc_guilds");
  }

  @Command(value = {"guild invite *", "guild invite all", "g invite *", "g invite all",
      "guild zapros *", "guild zapros all", "g zapros *",
      "g zapros all"}, description = "Zaprasza wszystkich graczy w obrębie 5 kratek do gildii.")
  public void handleGuildInviteAll(final @Sender Player sender) {
    final Location senderLocation = sender.getLocation();
    for (final Player receiver : this.plugin.getServer().getOnlinePlayers()) {
      if (sender.equals(receiver)) {
        continue;
      }

      if (senderLocation.distance(receiver.getLocation()) <= 5D) {
        this.handleGuildInvite(sender,
            this.plugin.getGuildUserFactory().findUserByUniqueId(receiver.getUniqueId())
                .orElseThrow(() -> new BladeExitMessage(ChatHelper.colored(
                    PlatformPlugin.getInstance()
                        .getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                            "{PLAYER_NAME}", receiver.getName())))));
      }
    }
  }
}
