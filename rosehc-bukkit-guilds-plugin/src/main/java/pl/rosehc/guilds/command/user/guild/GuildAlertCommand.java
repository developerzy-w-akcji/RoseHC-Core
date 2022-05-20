package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildAlertPacket;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserCooldownCache;

public final class GuildAlertCommand {

  private final GuildsPlugin plugin;

  public GuildAlertCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild alert",
      "g alert"}, description = "Wysyła powiadomienie na title do każdego członka online.")
  public void handleGuildAlert(final @Sender Player player,
      final @Name("text") @Combined String text) {
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
    if (cooldownCache.hasUserCooldown(PlatformUserCooldownType.GUILD_ALERT)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAlertYouAreCooldowned.replace(
              "{LEFT_TIME}", TimeHelper.timeToString(
                  cooldownCache.getUserCooldown(PlatformUserCooldownType.GUILD_ALERT)))));
    }

    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAlertYouDontHaveAnyGuild));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null || !guildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildAlertYouCannotSendAlert));
    }

    cooldownCache.putUserCooldown(PlatformUserCooldownType.GUILD_ALERT);
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildAlertPacket(guild.getTag(), text), "rhc_guilds");
  }
}
