package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildHomeLocationUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildSetHomeCommand {

  private final GuildsPlugin plugin;

  public GuildSetHomeCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild sethome", "g sethome", "guild setbase", "g setbase", "guild ustawdom",
      "guild ustawbaze", "g ustawbaze"}, description = "Ustawia nowy dom gildii.")
  public void handleGuildHome(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildSetHomeNoGuildFound));
    }

    final GuildMember guildMember = guild.getGuildMember(user);
    if (guildMember == null || !guildMember.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildSetHomeYouCannotManage));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildSetHomeSuccess);
    guild.setHomeLocation(player.getLocation());
    this.plugin.getRedisAdapter().sendPacket(new GuildHomeLocationUpdatePacket(guild.getTag(),
            SerializeHelper.serializeLocation(guild.getHomeLocation())), "rhc_master_controller",
        "rhc_guilds");
  }
}
