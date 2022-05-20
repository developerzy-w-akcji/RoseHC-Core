package pl.rosehc.guilds.command.user.user;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.guild.user.GuildUserSynchronizeRankingPacket;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserCooldownCache;

public final class UserRankingResetCommand {

  private final GuildsPlugin plugin;

  public UserRankingResetCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"rankingreset",
      "resetujranking"}, description = "Resetuje ranking dla podanego gracza.")
  public void handleRankingReset(final @Sender Player player) {
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
    if (cooldownCache.hasUserCooldown(PlatformUserCooldownType.USER_RANKING_RESET)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.userRankingResetIsCooldowned.replace(
              "{LEFT_TIME}", TimeHelper.timeToString(
                  cooldownCache.getUserCooldown(PlatformUserCooldownType.USER_RANKING_RESET)))));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.userRankingResetSuccess);
    cooldownCache.putUserCooldown(PlatformUserCooldownType.USER_RANKING_RESET);
    user.getUserRanking().reset();
    this.plugin.getRedisAdapter().sendPacket(
        new GuildUserSynchronizeRankingPacket(user.getUniqueId(), user.getUserRanking().getPoints(),
            user.getUserRanking().getKills(), user.getUserRanking().getDeaths(),
            user.getUserRanking().getKillStreak()), "rhc_master_controller", "rhc_guilds");
  }
}
