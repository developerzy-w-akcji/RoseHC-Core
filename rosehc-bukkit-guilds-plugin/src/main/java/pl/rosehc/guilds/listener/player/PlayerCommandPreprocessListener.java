package pl.rosehc.guilds.listener.player;

import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildPermissionType;

public final class PlayerCommandPreprocessListener implements Listener {

  private static final List<String> TELEPORT_REQUEST_COMMAND_LIST = Arrays.asList(
      "/teleportacacept", "/tpaccept",
      "/platform:tpaccept", "/platform:teleportaccept"
  );
  private final GuildsPlugin plugin;

  public PlayerCommandPreprocessListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCommand(final PlayerCommandPreprocessEvent event) {
    final String commandName = event.getMessage().split(" ")[0].toLowerCase();
    final Player player = event.getPlayer();
    if (player.hasPermission("guilds-region-bypass")) {
      return;
    }

    final boolean isTeleportRequestCommand = TELEPORT_REQUEST_COMMAND_LIST.contains(commandName);
    final boolean isBlockedCommand = this.plugin.getGuildsConfiguration().pluginWrapper.blockedTerrainCommandList.contains(
        commandName);
    if (isTeleportRequestCommand || isBlockedCommand) {
      this.plugin.getGuildUserFactory().findUserByUniqueId(player.getUniqueId()).ifPresent(user -> {
        final Guild userGuild = user.getGuild();
        if (isTeleportRequestCommand && userGuild != null && userGuild.getGuildRegion()
            .isInside(player.getLocation())) {
          final GuildMember member = userGuild.getGuildMember(user);
          if (member == null) {
            event.setCancelled(true);
            ChatHelper.sendMessage(player,
                this.plugin.getGuildsConfiguration().messagesWrapper.youCannotUseTeleportAcceptBecauseBadErrorOccured);
            return;
          }

          if (!member.hasPermission(GuildPermissionType.ACCEPTING_TELEPORTS_ON_TERRAIN)) {
            event.setCancelled(true);
            ChatHelper.sendMessage(player,
                this.plugin.getGuildsConfiguration().messagesWrapper.youCannotUseTeleportAcceptOnThisGuildBecauseYouDontHavePermission);
          }
        } else if (isBlockedCommand) {
          this.plugin.getGuildFactory().findGuildInside(player.getLocation())
              .filter(targetGuild -> userGuild == null || !userGuild.equals(targetGuild))
              .ifPresent(ignored -> {
                event.setCancelled(true);
                ChatHelper.sendMessage(player,
                    this.plugin.getGuildsConfiguration().messagesWrapper.youCannotUseThisCommandOnThisTerrain);
              });
        }
      });
    }
  }
}
