package pl.rosehc.guilds.command.user.guild;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildAllyInviteEntryUpdatePacket;
import pl.rosehc.controller.packet.guild.guild.GuildUpdateAllyPacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildAllyCommand {

  private final GuildsPlugin plugin;

  public GuildAllyCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild ally", "g ally", "guild sojusz",
      "g sojusz"}, description = "Wysyła (lub anuluje) zaproszenie do sojuszu z daną gildią.")
  public void handleGuildAlly(final @Sender Player player, final @Name("tag") Guild targetGuild) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild userGuild = user.getGuild();
    if (userGuild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyYouDontHaveAnyGuild));
    }

    if (targetGuild.equals(userGuild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyYouCannotInviteYourOwnGuild));
    }

    final GuildMember guildMember = userGuild.getGuildMember(user);
    if (guildMember == null || !guildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyYouCannotInviteOtherGuild));
    }

    if (userGuild.getAlliedGuild() != null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyYouAlreadyHaveOtherAlly));
    }

    if (targetGuild.getAlliedGuild() != null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyTargetAlreadyHaveOtherAlly));
    }

    final Entry<String, Long> existingUserAllyInviteEntry = userGuild.getAllyInviteEntry();
    if (existingUserAllyInviteEntry != null && existingUserAllyInviteEntry.getKey()
        .equals(targetGuild.getTag())
        && existingUserAllyInviteEntry.getValue() > System.currentTimeMillis()) {
      ChatHelper.sendMessage(player,
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyInviteCancelledSender.replace(
              "{TAG}", targetGuild.getTag()).replace("{NAME}", targetGuild.getName()));
      targetGuild.broadcastChatMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllyInviteCancelledReceiver.replace(
              "{TAG}", userGuild.getTag()).replace("{NAME}", targetGuild.getName()));
      targetGuild.setAlliedGuild(userGuild);
      userGuild.setAlliedGuild(targetGuild);
      userGuild.setAllyInviteEntry(null);
      this.plugin.getRedisAdapter().sendPacket(
          new GuildAllyInviteEntryUpdatePacket(userGuild.getTag(), userGuild.getAllyInviteEntry()),
          "rhc_master_controller", "rhc_guilds");
      return;
    }

    final Entry<String, Long> existingTargetAllyInviteEntry = targetGuild.getAllyInviteEntry();
    if (existingTargetAllyInviteEntry != null && existingTargetAllyInviteEntry.getKey()
        .equals(userGuild.getTag())
        && existingTargetAllyInviteEntry.getValue() > System.currentTimeMillis()) {
      ChatHelper.sendMessage(player,
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySuccessSender.replace(
              "{TAG}", targetGuild.getTag()).replace("{NAME}", targetGuild.getName()));
      userGuild.broadcastChatMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySuccessGuildSender.replace(
              "{TAG}", targetGuild.getTag()).replace("{NAME}", targetGuild.getName()));
      targetGuild.broadcastChatMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySuccessGuildReceiver.replace(
              "{TAG}", userGuild.getTag()).replace("{NAME}", userGuild.getName()));
      targetGuild.setAlliedGuild(userGuild);
      userGuild.setAlliedGuild(targetGuild);
      targetGuild.setAllyInviteEntry(null);
      this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySuccessBroadcast.replace(
                  "{FIRST_TAG}", userGuild.getTag()).replace("{FIRST_NAME}", userGuild.getName())
              .replace("{SECOND_TAG}", targetGuild.getTag())
              .replace("{SECOND_NAME}", targetGuild.getName()), false), "rhc_platform");
      this.plugin.getRedisAdapter()
          .sendPacket(new GuildUpdateAllyPacket(targetGuild.getTag(), userGuild.getTag(), true),
              "rhc_master_controller", "rhc_Platform");
      this.plugin.getRedisAdapter().sendPacket(
          new GuildAllyInviteEntryUpdatePacket(targetGuild.getTag(),
              targetGuild.getAllyInviteEntry()), "rhc_master_controller", "rhc_guilds");
      return;
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySentSender.replace("{TAG}",
            targetGuild.getTag()).replace("{NAME}", targetGuild.getName()));
    userGuild.setAllyInviteEntry(new SimpleEntry<>(targetGuild.getTag(), System.currentTimeMillis()
        + this.plugin.getGuildsConfiguration().pluginWrapper.parsedGuildAllyInviteConsiderationTimeoutTime));
    userGuild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySentGuildSender.replace(
            "{TAG}", targetGuild.getTag()).replace("{NAME}", targetGuild.getName()));
    targetGuild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildAllySentGuildReceiver.replace(
            "{TAG}", userGuild.getTag()).replace("{NAME}", userGuild.getName()));
    this.plugin.getRedisAdapter().sendPacket(
        new GuildAllyInviteEntryUpdatePacket(userGuild.getTag(), userGuild.getAllyInviteEntry()),
        "rhc_master_controller", "rhc_guilds");
  }
}
