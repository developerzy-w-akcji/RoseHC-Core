package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildHelpInfoAddPacket;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildPlayerHelpInfo;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserCooldownCache;

public final class GuildNeedHelpCommand {

  private final GuildsPlugin plugin;

  public GuildNeedHelpCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild needhelp", "g needhelp", "guild potrzebujepomocy", "g potrzebujepomocy",
      "guild pp", "g pp"}, description = "Wysyła prośbę o pomoc do gildii.")
  public void handleGuildNeedHelp(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final PlatformUserCooldownCache cooldownCache = PlatformPlugin.getInstance()
        .getPlatformUserFactory().findUserByUniqueId(player.getUniqueId())
        .map(PlatformUser::getCooldownCache).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    if (cooldownCache.hasUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpIsCooldownedGuild.replace(
              "{LEFT_TIME}", TimeHelper.timeToString(
                  cooldownCache.getUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP)))));
    }

    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpYouDontHaveAnyGuild));
    }

    if (guild.getGuildPlayerHelpInfoMap() != null && guild.getGuildPlayerHelpInfoMap().size()
        >= this.plugin.getGuildsConfiguration().pluginWrapper.guildNeedHelpNormalLimit) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpTooManyHelpsGuild));
    }

    final long time = System.currentTimeMillis()
        + this.plugin.getGuildsConfiguration().pluginWrapper.parsedGuildNeedHelpWaypointTime;
    final Location location = player.getLocation();
    final GuildPlayerHelpInfo helpInfo = new GuildPlayerHelpInfo(player.getName(), time,
        location.getBlockX(), location.getBlockY(), location.getBlockZ());
    guild.addGuildPlayerHelpInfo(player.getUniqueId(), helpInfo);
    cooldownCache.putUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP);
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpRequestSuccessGuild);
    this.plugin.getRedisAdapter().sendPacket(
        new GuildHelpInfoAddPacket(guild.getTag(), player.getUniqueId(), player.getName(), time,
            helpInfo.getX(), helpInfo.getY(), helpInfo.getZ(), false), "rhc_master_controller",
        "rhc_guilds");
  }

  @Command(value = {"guild needhelpa", "g needhelpa", "guild potrzebujepomocya",
      "g potrzebujepomocya", "guild ppa",
      "g ppa"}, description = "Wysyła prośbę o pomoc do gildii i sojuszy.")
  public void handleGuildNeedHelpAlly(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final PlatformUserCooldownCache cooldownCache = PlatformPlugin.getInstance()
        .getPlatformUserFactory().findUserByUniqueId(player.getUniqueId())
        .map(PlatformUser::getCooldownCache).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    if (cooldownCache.hasUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP_ALLY)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpIsCooldownedAlly.replace(
              "{LEFT_TIME}", TimeHelper.timeToString(
                  cooldownCache.getUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP_ALLY)))));
    }

    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpYouDontHaveAnyGuild));
    }

    if (guild.getAllyPlayerHelpInfoMap() != null && guild.getAllyPlayerHelpInfoMap().size()
        >= this.plugin.getGuildsConfiguration().pluginWrapper.guildNeedHelpAllyLimit) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpTooManyHelpsAlly));
    }

    final long time = System.currentTimeMillis()
        + this.plugin.getGuildsConfiguration().pluginWrapper.parsedGuildNeedHelpWaypointTime;
    final Location location = player.getLocation();
    final GuildPlayerHelpInfo helpInfo = new GuildPlayerHelpInfo(player.getName(), time,
        location.getBlockX(), location.getBlockY(), location.getBlockZ());
    guild.addGuildAllyPlayerHelpInfo(player.getUniqueId(), helpInfo);
    cooldownCache.putUserCooldown(PlatformUserCooldownType.GUILD_NEED_HELP_ALLY);
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildNeedHelpRequestSuccessAlly);
    this.plugin.getRedisAdapter().sendPacket(
        new GuildHelpInfoAddPacket(guild.getTag(), player.getUniqueId(), player.getName(), time,
            helpInfo.getX(), helpInfo.getY(), helpInfo.getZ(), true), "rhc_master_controller",
        "rhc_guilds");
  }
}
