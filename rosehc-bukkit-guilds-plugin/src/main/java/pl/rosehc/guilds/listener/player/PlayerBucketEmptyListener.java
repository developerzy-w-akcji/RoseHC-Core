package pl.rosehc.guilds.listener.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.vanishingblock.VanishingBlock;

public final class PlayerBucketEmptyListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerBucketEmptyListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEmpty(final PlayerBucketEmptyEvent event) {
    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    final Block blockPlaced = event.getBlockClicked().getRelative(event.getBlockFace());
    final Material bucket = event.getBucket();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(
        user -> this.plugin.getGuildFactory().findGuildInside(blockPlaced.getLocation())
            .ifPresent(guild -> {
              if (user.getGuild() == null || !user.getGuild().equals(guild)) {
                if (!bucket.equals(Material.WATER_BUCKET)) {
                  event.setCancelled(true);
                  ChatHelper.sendMessage(player,
                      this.plugin.getGuildsConfiguration().messagesWrapper.youCannotEmptyBucketsOnEnemyGuild);
                  return;
                }

                PlatformPlugin.getInstance().getVanishingBlockFactory().vanish(
                    new VanishingBlock(blockPlaced.getLocation(), Material.STATIONARY_WATER,
                        System.currentTimeMillis() + 5000L));
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.waterWillBeVanishedSoon);
                return;
              }

              final GuildMember member = guild.getGuildMember(user);
              if (member == null) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotEmptyBucketBecauseBadErrorOccurred);
                return;
              }

              if (!member.hasPermission(
                  bucket.equals(Material.WATER_BUCKET) ? GuildPermissionType.WATER_PLACING_ACCESS
                      : GuildPermissionType.LAVA_PLACING_ACCESS)) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player, bucket.equals(Material.WATER_BUCKET)
                    ? this.plugin.getGuildsConfiguration().messagesWrapper.youCannotEmptyWaterBecauseYouDontHavePermission
                    : this.plugin.getGuildsConfiguration().messagesWrapper.youCannotEmptyLavaBecauseYouDontHavePermission);
              }
            }));
  }
}
