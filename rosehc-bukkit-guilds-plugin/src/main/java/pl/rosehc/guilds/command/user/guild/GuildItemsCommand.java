package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.entity.Player;
import pl.rosehc.guilds.inventories.GuildItemsInventory;
import pl.rosehc.guilds.inventories.GuildTypeSelectionInventory;

public final class GuildItemsCommand {

  @Command(value = {"guild items", "g items", "guild itemy",
      "g itemy"}, description = "Otwiera GUI od itemów na gildie danego typu.")
  public void handleGuildItems(final @Sender Player player) {
    final GuildTypeSelectionInventory selectionInventory = new GuildTypeSelectionInventory(player,
        type -> {
          final GuildItemsInventory itemsInventory = new GuildItemsInventory(player, type);
          itemsInventory.open();
        });
    selectionInventory.open();
  }
}
