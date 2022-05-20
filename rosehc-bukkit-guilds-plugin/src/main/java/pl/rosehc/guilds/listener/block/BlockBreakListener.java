package pl.rosehc.guilds.listener.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildPistonsUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;

public final class BlockBreakListener implements Listener {

  private final GuildsPlugin plugin;

  public BlockBreakListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBreak(final BlockBreakEvent event) {
    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    final Block block = event.getBlock();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(
        user -> this.plugin.getGuildFactory().findGuildInside(block.getLocation())
            .ifPresent(guild -> {
              if (user.getGuild() == null || !user.getGuild().equals(guild)) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakBlocksOnEnemyGuild);
                return;
              }

              final Material type = block.getType();
              if (guild.getPistonBlockScanner() != null && guild.getPistonBlockScanner()
                  .getMaterialsToScanSet().contains(type)) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakPistonsWhenPistonScannerIsEnabled);
                return;
              }

              if (type.name().contains("PISTON")) {
                guild.setPistonsOnGuild(guild.getPistonsOnGuild() - 1);
                this.plugin.getRedisAdapter().sendPacket(
                    new GuildPistonsUpdatePacket(guild.getTag(), guild.getPistonsOnGuild()),
                    "rhc_master_controller", "rhc_guilds");
                return;
              }

              if (guild.getGuildRegion().isInsideCenter(block.getLocation())) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakBlocksInCenter);
                return;
              }

              if (guild.canNotBuild()) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakBlocksNowBecauseTntExploded);
                return;
              }

              final GuildMember guildMember = guild.getGuildMember(user);
              if (guildMember == null) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakBlocksBecauseBadErrorOccurred);
                return;
              }

              if (!guildMember.hasPermission(GuildPermissionType.BREAKING_BLOCKS)) {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotBreakBlocksBecauseYouDontHavePermission);
              }
            }));
  }
}
