package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;

public final class GuildHelpCommand {

  private final GuildsPlugin plugin;

  public GuildHelpCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild", "guild help", "g", "g help", "guild pomoc",
      "g pomoc"}, description = "Wyświetla pomoc gildyjną.")
  public void handleGuildHelp(final @Sender Player player) {
    ChatHelper.sendMessage(player, this.plugin.getGuildsConfiguration().messagesWrapper.guildHelp);
  }
}
