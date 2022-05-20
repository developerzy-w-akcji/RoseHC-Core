package pl.rosehc.guilds.listener.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.guild.guild.GuildPistonsUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;
import pl.rosehc.guilds.guild.GuildType;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;

public final class BlockPlaceListener implements Listener {

  private final GuildsPlugin plugin;

  public BlockPlaceListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlace(final BlockPlaceEvent event) {
    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    final Block blockPlaced = event.getBlockPlaced();
    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
      final Guild userGuild = user.getGuild();
      if (userGuild != null) {
        final GuildType guildType = userGuild.getGuildType();
        if (blockPlaced.getY() > guildType.getMaxBlockPlaceY() && PlatformPlugin.getInstance()
            .getPlatformUserFactory().findUserByUniqueId(player.getUniqueId())
            .filter(PlatformUser::isInCombat).isPresent()) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player, guildType.getMaxBlockPlaceY() > 0
              ? this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksOnThisYWhileInCombat.replace(
              "{MAX_BLOCK_Y}", String.valueOf(guildType.getMaxBlockPlaceY()))
              : this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksWhileInCombat);
          return;
        }
      }

      this.plugin.getGuildFactory().findGuildInside(blockPlaced.getLocation()).ifPresent(guild -> {
        if (userGuild == null || !userGuild.equals(guild)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksOnEnemyGuild);
          return;
        }

        final Material type = blockPlaced.getType();
        if (guild.getPistonBlockScanner() != null && guild.getPistonBlockScanner()
            .getMaterialsToScanSet().contains(type)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlacePistonsWhenPistonScannerIsEnabled);
          return;
        }

        if (type.name().contains("PISTON")) {
          if (guild.getPistonsOnGuild()
              >= this.plugin.getGuildsConfiguration().pluginWrapper.pistonLimit) {
            event.setCancelled(true);
            ChatHelper.sendMessage(player,
                this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlacePistonsBecauseLimitHasBeenReached);
            return;
          }

          guild.setPistonsOnGuild(guild.getPistonsOnGuild() + 1);
          this.plugin.getRedisAdapter()
              .sendPacket(new GuildPistonsUpdatePacket(guild.getTag(), guild.getPistonsOnGuild()),
                  "rhc_master_controller", "rhc_guilds");
          return;
        }

        if (guild.getGuildRegion().isInsideCenter(blockPlaced.getLocation())) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksInCenter);
          return;
        }

        if (guild.canNotBuild()) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksNowBecauseTntExploded);
          return;
        }

        final GuildMember guildMember = guild.getGuildMember(user);
        if (guildMember == null) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksBecauseBadErrorOccurred);
          return;
        }

        if (!guildMember.hasPermission(GuildPermissionType.PLACING_BLOCKS)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceBlocksOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.COAL_BLOCK && !guildMember.hasPermission(
            GuildPermissionType.PLACING_COAL)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceCoalOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.LAPIS_BLOCK && !guildMember.hasPermission(
            GuildPermissionType.PLACING_LAPIS_BLOCKS)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceLapisOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.OBSIDIAN && !guildMember.hasPermission(
            GuildPermissionType.PLACING_OBSIDIAN)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceObsidianOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.SAND && !guildMember.hasPermission(GuildPermissionType.PLACING_SAND)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceSandOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if ((type.name().contains("REDSTONE") || type.name().contains("PISTON") || type.name()
            .contains("PLATE") || type == Material.DISPENSER || type == Material.DROPPER)
            && !guildMember.hasPermission(GuildPermissionType.REDSTONE_PLACING_ACCESS)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceRedStoneOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.ANVIL && !guildMember.hasPermission(
            GuildPermissionType.ANVIL_PLACING_ACCESS)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceAnvilOnThisGuildBecauseYouDontHavePermission);
          return;
        }

        if (type == Material.TNT && !guildMember.hasPermission(GuildPermissionType.PLACING_TNT)) {
          event.setCancelled(true);
          ChatHelper.sendMessage(player,
              this.plugin.getGuildsConfiguration().messagesWrapper.youCannotPlaceTntOnThisGuildBecauseYouDontHavePermission);
        }
      });
    });
  }
}
