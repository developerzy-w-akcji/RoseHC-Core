package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.rosehc.guilds.GuildsPlugin;

public final class GuildPanelCommand {

  private final GuildsPlugin plugin;

  public GuildPanelCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild panel", "g panel"}, description = "Otwiera GUI od panelu gildii.")
  public void handleGuildPanel(final @Sender Player player) {
    player.sendMessage(ChatColor.RED + "panel off");
  }
}
