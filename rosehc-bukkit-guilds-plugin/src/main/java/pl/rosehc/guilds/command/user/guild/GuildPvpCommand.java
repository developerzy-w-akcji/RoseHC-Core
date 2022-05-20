package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildPvPUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildPvpCommand {

  private final GuildsPlugin plugin;

  public GuildPvpCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild pvp", "guild ff", "g pvp",
      "g ff"}, description = "Zmienia status PVP w gildii.")
  public void handleGuildPvPInGuild(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpNoGuildFound));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null || !guildMember.hasPermission(
        GuildPermissionType.CHANGING_PVP_STATE_IN_GUILD)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpCannotChangeStatusInGuild));
    }

    guild.setPvpGuild(!guild.isPvpGuild());
    ChatHelper.sendMessage(player, !guild.isPvpGuild()
        ? this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpSuccessfullyDisabledInGuild
        : this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpSuccessfullyEnabledInGuild);
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildPvPUpdatePacket(guild.getTag(), guild.isPvpGuild(), false),
            "rhc_master_controller", "rhc_guilds");
  }

  @Command(value = {"guild pvpa", "guild ffa", "g pvpa",
      "g ffa"}, description = "Zmienia status PVP w sojuszach.")
  public void handleGuildPvPInAlly(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpNoGuildFound));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null || !guildMember.hasPermission(
        GuildPermissionType.CHANGING_PVP_STATE_IN_ALLY)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpCannotChangeStatusInAlly));
    }

    guild.setPvpAlly(!guild.isPvpAlly());
    ChatHelper.sendMessage(player, !guild.isPvpAlly()
        ? this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpSuccessfullyDisabledInAlly
        : this.plugin.getGuildsConfiguration().messagesWrapper.guildPvpSuccessfullyEnabledInAlly);
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildPvPUpdatePacket(guild.getTag(), guild.isPvpAlly(), true),
            "rhc_master_controller", "rhc_guilds");
  }
}
