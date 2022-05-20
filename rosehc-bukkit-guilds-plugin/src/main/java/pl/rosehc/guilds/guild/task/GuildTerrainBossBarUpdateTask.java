package pl.rosehc.guilds.guild.task;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.bossbar.BossBarBuilder;
import pl.rosehc.bossbar.BossBarPlugin;
import pl.rosehc.bossbar.user.UserBar;
import pl.rosehc.bossbar.user.UserBarConstants;
import pl.rosehc.bossbar.user.UserBarType;
import pl.rosehc.guilds.GuildsConfiguration.MessagesWrapper.BossBarMessageWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildTerrainBossBarUpdateTask implements Runnable {

  private final GuildsPlugin plugin;

  public GuildTerrainBossBarUpdateTask(final GuildsPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, this, 10L, 10L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
        final Guild enteredGuild = user.getEnteredGuild();
        if (enteredGuild == null) {
          BossBarPlugin.getInstance().getUserBarFactory().findUserBar(player)
              .filter(userBar -> userBar.hasBossBar(UserBarType.GUILD))
              .ifPresent(userBar -> userBar.removeBossBar(UserBarType.GUILD));
          return;
        }

        final UserBar userBar = BossBarPlugin.getInstance().getUserBarFactory().getUserBar(player);
        final BossBarMessageWrapper bossBarFromRelation = this.getBossBarFromRelation(user,
            enteredGuild);
        final Location location = player.getLocation();
        location.setY(enteredGuild.getGuildRegion().getCenterLocation().getY());
        if (!userBar.hasBossBar(UserBarType.GUILD)) {
          userBar.addBossBar(UserBarType.GUILD, BossBarBuilder.add(UserBarConstants.GUILD_UUID)
              .title(TextComponent.fromLegacyText(ChatHelper.colored(
                  (enteredGuild.getProtectionTime() > System.currentTimeMillis()
                      ? bossBarFromRelation.title.replace("{TIME}", TimeHelper.timeToString(
                      enteredGuild.getProtectionTime() - System.currentTimeMillis()))
                      : bossBarFromRelation.title).replace("{TAG}", enteredGuild.getTag()))))
              .progress(
                  (float) (location.distance(enteredGuild.getGuildRegion().getCenterLocation()) / (
                      enteredGuild.getGuildRegion().getSize() / 2)))
              .color(bossBarFromRelation.barColorWrapper.toOriginal())
              .style(bossBarFromRelation.barStyleWrapper.toOriginal()));
          return;
        }

        userBar.updateBossBar(UserBarType.GUILD, ChatHelper.colored(
                (enteredGuild.getProtectionTime() > System.currentTimeMillis()
                    ? bossBarFromRelation.title.replace("{TIME}", TimeHelper.timeToString(
                    enteredGuild.getProtectionTime() - System.currentTimeMillis()))
                    : bossBarFromRelation.title).replace("{TAG}", enteredGuild.getTag())),
            (float) (location.distance(enteredGuild.getGuildRegion().getCenterLocation()) / (
                enteredGuild.getGuildRegion().getSize() / 2)),
            bossBarFromRelation.barColorWrapper.toOriginal(),
            bossBarFromRelation.barStyleWrapper.toOriginal());
      });
    }
  }

  private BossBarMessageWrapper getBossBarFromRelation(final GuildUser user,
      final Guild targetGuild) {
    final Guild guild = user.getGuild();
    if (guild != null) {
      final Guild alliedGuild = guild.getAlliedGuild();
      if (alliedGuild != null && alliedGuild.equals(targetGuild)) {
        return targetGuild.getProtectionTime() <= System.currentTimeMillis()
            ? this.plugin.getGuildsConfiguration().messagesWrapper.allyGuildWithoutTntProtectionBossBarMessage
            : this.plugin.getGuildsConfiguration().messagesWrapper.allyGuildWithTntProtectionBossBarMessage;
      }

      if (targetGuild.equals(guild)) {
        return targetGuild.getProtectionTime() <= System.currentTimeMillis()
            ? this.plugin.getGuildsConfiguration().messagesWrapper.yourGuildWithoutTntProtectionBossBarMessage
            : this.plugin.getGuildsConfiguration().messagesWrapper.yourGuildWithTntProtectionBossBarMessage;
      }
    }

    return targetGuild.getProtectionTime() <= System.currentTimeMillis()
        ? this.plugin.getGuildsConfiguration().messagesWrapper.enemyGuildWithoutTntProtectionBossBarMessage
        : this.plugin.getGuildsConfiguration().messagesWrapper.enemyGuildWithTntProtectionBossBarMessage;
  }
}
