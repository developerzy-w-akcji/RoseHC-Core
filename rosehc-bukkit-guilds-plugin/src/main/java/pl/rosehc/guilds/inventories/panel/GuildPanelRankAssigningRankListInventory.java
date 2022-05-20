package pl.rosehc.guilds.inventories.panel;

import java.util.Map.Entry;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.group.GuildGroup;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildPanelRankAssigningRankListInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildPanelRankAssigningRankListInventory(final Player player, final Guild guild,
      final int page) {
    this.player = player;
    final SpigotGuiWrapper guildRankAssigningInventoryWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("panel_rank_assigning_rank_list");
    if (guildRankAssigningInventoryWrapper == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = new BukkitInventory(
        ChatHelper.colored(guildRankAssigningInventoryWrapper.inventoryName),
        guildRankAssigningInventoryWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildRankAssigningInventoryWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : guildRankAssigningInventoryWrapper.elements.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("rank_info") && !entry.getKey()
          .equalsIgnoreCase("back")) {
        this.inventory.setElement(entry.getValue().slot,
            new BukkitInventoryElement(entry.getValue().asItemStack()));
      }
    }

    final SpigotGuiElementWrapper rankInfoElementWrapper = guildRankAssigningInventoryWrapper.elements.get(
        "rank_info");
    if (rankInfoElementWrapper instanceof DefaultSpigotGuiElementWrapper) {
      final DefaultSpigotGuiElementWrapper rankInfoElement = (DefaultSpigotGuiElementWrapper) rankInfoElementWrapper;
      for (final GuildGroup group : guild.getGuildGroupMap().values()) {
        this.inventory.addElement(new BukkitInventoryElement(new ItemStackBuilder(
            new ItemStack(Material.STAINED_CLAY, 1,
                (short) group.getColor().getStainedClayData())).withName(
            group.getColor().getChatColor() + group.getName()).build(), event -> {

        }));
      }
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
