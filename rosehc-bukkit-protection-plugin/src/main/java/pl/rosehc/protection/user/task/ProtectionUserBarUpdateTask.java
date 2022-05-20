package pl.rosehc.protection.user.task;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.bossbar.BossBarBuilder;
import pl.rosehc.bossbar.BossBarPlugin;
import pl.rosehc.bossbar.user.UserBar;
import pl.rosehc.bossbar.user.UserBarType;
import pl.rosehc.protection.ProtectionPlugin;

public final class ProtectionUserBarUpdateTask implements Runnable {

  private final ProtectionPlugin plugin;

  public ProtectionUserBarUpdateTask(final ProtectionPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this, 20L, 20L);
  }

  @Override
  public void run() {
    for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
      this.plugin.getProtectionUserFactory().findUser(player).ifPresent(user -> {
        if (!user.hasExpired()) {
          final UserBar userBar = BossBarPlugin.getInstance().getUserBarFactory()
              .getUserBar(player);
          if (!userBar.hasBossBar(UserBarType.PROTECTION)) {
            userBar.addBossBar(UserBarType.PROTECTION,
                BossBarBuilder.add(UserBarType.PROTECTION.getUniqueId())
                    .color(this.plugin.getProtectionConfiguration().barColorWrapper.toOriginal())
                    .progress(1F)
                    .style(this.plugin.getProtectionConfiguration().barStyleWrapper.toOriginal())
                    .title(TextComponent.fromLegacyText(ChatHelper.colored(
                        this.plugin.getProtectionConfiguration().protectionExpiryTimeInformation.replace(
                            "{TIME}", TimeHelper.timeToString(
                                user.getExpiryTime() - System.currentTimeMillis()))))));
            return;
          }

          userBar.updateBossBar(UserBarType.PROTECTION, ChatHelper.colored(
              this.plugin.getProtectionConfiguration().protectionExpiryTimeInformation.replace(
                  "{TIME}",
                  TimeHelper.timeToString(user.getExpiryTime() - System.currentTimeMillis()))));
        } else {
          BossBarPlugin.getInstance().getUserBarFactory().findUserBar(player)
              .ifPresent(userBar -> userBar.removeBossBar(UserBarType.PROTECTION));
        }
      });
    }
  }
}
