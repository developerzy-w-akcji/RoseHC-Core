package pl.rosehc.guilds.listener.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.platform.PlatformPlugin;

public final class PlayerBucketFillListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerBucketFillListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onFill(final PlayerBucketFillEvent event) {
    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    final Block blockClicked = event.getBlockClicked();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).flatMap(
            user -> this.plugin.getGuildFactory().findGuildInside(blockClicked.getLocation())
                .filter(guild -> user.getGuild() == null || !user.getGuild().equals(guild)))
        .ifPresent(guild -> {
          if (PlatformPlugin.getInstance().getVanishingBlockFactory()
              .unVanish(blockClicked.getLocation(), Material.STATIONARY_WATER)) {
            ChatHelper.sendMessage(player,
                this.plugin.getGuildsConfiguration().messagesWrapper.waterWasSuccessfullyBorrowed);
            return;
          }

          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotFillBucketsOnEnemyGuild);
          event.setCancelled(true);
        });
  }
}
