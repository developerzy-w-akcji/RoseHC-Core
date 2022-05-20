package pl.rosehc.guilds.command.user.guild;

import java.util.ArrayList;
import java.util.Collections;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildMemberRemovePacket;
import pl.rosehc.controller.packet.guild.user.GuildUserTeleportOutFromTerrainPacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserMessagePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;

public final class GuildKickCommand {

  private final GuildsPlugin plugin;

  public GuildKickCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild kick", "g kick", "guild wyrzuc",
      "g wyrzuc"}, description = "Wyrzuca podanego gracza z gildii.")
  public void handleGuildKick(final @Sender Player player,
      final @Name("nickname") GuildUser receiverUser) {
    final GuildUser senderUser = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild senderGuild = senderUser.getGuild();
    if (senderGuild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickYouDontHaveAnyGuild));
    }

    final Guild receiverGuild = receiverUser.getGuild();
    if (receiverGuild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickTargetDontHaveAnyGuild));
    }

    if (!receiverGuild.equals(senderGuild)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickTargetIsNotInYourGuild));
    }

    final GuildMember senderGuildMember = senderGuild.getGuildMember(senderUser);
    if (senderGuildMember == null || !senderGuildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickYouCannotKickAnyone));
    }

    final GuildMember receiverGuildMember = receiverGuild.getGuildMember(receiverUser);
    if (receiverGuildMember == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickBadErrorOccurred));
    }

    if (receiverGuildMember.getUniqueId().equals(senderGuildMember.getUniqueId())) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickYouCannotKickYourself));
    }

    if (receiverGuildMember.isLeader()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildKickYouCannotKickLeader));
    }

    receiverGuild.removeGuildMember(receiverUser);
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildKickSuccessSender.replace(
            "{PLAYER_NAME}", receiverUser.getNickname()));
    SectorsPlugin.getInstance().getSectorUserFactory()
        .findUserByUniqueId(receiverUser.getUniqueId()).ifPresent(receiverSectorUser -> {
          this.plugin.getRedisAdapter().sendPacket(
              new GuildUserTeleportOutFromTerrainPacket(receiverGuild.getTag(),
                  receiverSectorUser.getUniqueId()),
              "rhc_guilds_" + receiverSectorUser.getSector().getName());
          this.plugin.getRedisAdapter().sendPacket(new PlatformUserMessagePacket(
                  new ArrayList<>(Collections.singletonList(receiverUser.getUniqueId())),
                  this.plugin.getGuildsConfiguration().messagesWrapper.guildKickSuccessReceiver.replace(
                          "{TAG}", receiverGuild.getTag()).replace("{NAME}", receiverGuild.getName())
                      .replace("{PLAYER_NAME}", player.getName())),
              "rhc_platform_" + receiverSectorUser.getSector().getName());
        });
    this.plugin.getRedisAdapter().sendPacket(
        new GuildMemberRemovePacket(receiverGuild.getTag(), receiverGuildMember.getUniqueId()),
        "rhc_master_controller", "rhc_guilds");
    this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildKickSuccessBroadcast.replace(
                "{TAG}", receiverGuild.getTag()).replace("{NAME}", receiverGuild.getName())
            .replace("{RECEIVER_PLAYER_NAME}", receiverUser.getNickname())
            .replace("{SENDER_PLAYER_NAME}", player.getName()), false), "rhc_platform");
  }
}
