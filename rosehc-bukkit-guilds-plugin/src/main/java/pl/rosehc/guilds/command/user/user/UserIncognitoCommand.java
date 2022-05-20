package pl.rosehc.guilds.command.user.user;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserCooldownCache;

public final class UserIncognitoCommand {

  private final GuildsPlugin plugin;

  public UserIncognitoCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("guilds-command-incognito")
  @Command(value = "incognito", description = "Włącza lub wyłącza incognito")
  public void handleIncognito(final @Sender Player player) {
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
    if (cooldownCache.hasUserCooldown(PlatformUserCooldownType.USER_INCOGNITO)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.userIncognitoIsCooldowned.replace(
              "{LEFT_TIME}", TimeHelper.timeToString(
                  cooldownCache.getUserCooldown(PlatformUserCooldownType.USER_INCOGNITO)
                      - System.currentTimeMillis()))));
    }
  }
}
