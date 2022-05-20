package pl.rosehc.guilds.listener.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;

public final class PlayerInteractListener implements Listener {

  private final GuildsPlugin plugin;

  public PlayerInteractListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInteract(final PlayerInteractEvent event) {
    if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }

    final Block clickedBlock = event.getClickedBlock();
    final Material clickedBlockType = clickedBlock.getType();
    if (clickedBlockType != Material.CHEST && clickedBlockType != Material.FURNACE) {
      return;
    }

    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
      final Guild guild = user.getGuild();
      if (guild == null || !guild.getGuildRegion().isInside(clickedBlock.getLocation())) {
        return;
      }

      final GuildMember member = guild.getGuildMember(user);
      final boolean isChest = clickedBlock.getType().equals(Material.CHEST);
      if (member == null) {
        event.setCancelled(true);
        ChatHelper.sendMessage(player, isChest
            ? this.plugin.getGuildsConfiguration().messagesWrapper.youCannotOpenChestsBecauseBadErrorOccured
            : this.plugin.getGuildsConfiguration().messagesWrapper.youCannotOpenFurnacesBecauseBadErrorOccured);
        return;
      }

      if (!member.hasPermission(
          isChest ? GuildPermissionType.CHEST_ACCESS : GuildPermissionType.FURNACES_ACCESS)) {
        event.setCancelled(true);
        ChatHelper.sendMessage(player, isChest
            ? this.plugin.getGuildsConfiguration().messagesWrapper.youCannotOpenChestsBecauseYouDontHavePermission
            : this.plugin.getGuildsConfiguration().messagesWrapper.youCannotOpenFurnacesBecauseYouDontHavePermission);
      }
    });
  }
}
