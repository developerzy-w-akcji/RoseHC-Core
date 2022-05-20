package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildDeletePacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildDeleteCommand {

  private final GuildsPlugin plugin;

  public GuildDeleteCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild delete", "g delete",
      "g usun"}, description = "Usuwa gildię podanego gracza.")
  public void handle(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeleteYouDontHaveAnyGuild));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null || !guildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeleteYouAreNotALeader));
    }

    if (user.isPreparedForGuildDeletion()) {
      for (final GuildMember member : guild.getGuildMembers()) {
        if (member != null) {
          member.getUser().setGuild(null);
          member.getUser().setMemberArrayPosition(-1);
        }
      }

      this.plugin.getGuildFactory().unregisterGuild(guild);
      final Guild alliedGuild = guild.getAlliedGuild();
      if (alliedGuild != null) {
        alliedGuild.setAlliedGuild(null);
      }

      this.plugin.getRedisAdapter()
          .sendPacket(new GuildDeletePacket(guild.getTag()), "rhc_master_controller", "rhc_guilds");
      this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildDeleteSucceedBroadcast.replace(
                  "{NAME}", guild.getName()).replace("{TAG}", guild.getTag())
              .replace("{PLAYER_NAME}", player.getName()), false), "rhc_platform");
      if (guild.getPistonBlockScanner() != null) {
        guild.getPistonBlockScanner().cancel();
      }

      user.setPreparedForGuildDeletion(false);
      return;
    }

    user.setPreparedForGuildDeletion(true);
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildDeleteDeletionPrepared);
  }
}
