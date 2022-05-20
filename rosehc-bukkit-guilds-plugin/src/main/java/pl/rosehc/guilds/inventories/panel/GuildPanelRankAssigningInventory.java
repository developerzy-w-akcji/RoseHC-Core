package pl.rosehc.guilds.inventories.panel;

import java.util.Map.Entry;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.platform.PlatformPlugin;

public final class GuildPanelRankAssigningInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public GuildPanelRankAssigningInventory(final Player player, final Guild guild, final int page) {
    this.player = player;
    final SpigotGuiWrapper guildRankAssigningInventoryWrapper = GuildsPlugin.getInstance()
        .getGuildsConfiguration().inventoryMap.get("panel_rank_assigning");
    if (guildRankAssigningInventoryWrapper == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = new BukkitInventory(ChatHelper.colored(
        guildRankAssigningInventoryWrapper.inventoryName.replace("{PAGE}", String.valueOf(page))),
        guildRankAssigningInventoryWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = guildRankAssigningInventoryWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : guildRankAssigningInventoryWrapper.elements.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("member") && !entry.getKey().equalsIgnoreCase("back")) {
        this.inventory.setElement(entry.getValue().slot,
            new BukkitInventoryElement(entry.getValue().asItemStack()));
      }
    }

//    final List<GuildMember> members = Iterables.get(paginatedMemberList, page - 1, null);
//    if (members != null) {
//      final SpigotGuiElementWrapper memberElementWrapper = guildRankAssigningInventoryWrapper.elements.get("member");
//      if (memberElementWrapper instanceof DefaultSpigotGuiElementWrapper) {
//        final DefaultSpigotGuiElementWrapper memberElement = new DefaultSpigotGuiElementWrapper();
//        for (final GuildMember member : members) {
//          this.inventory.addElement(new BukkitInventoryElement(new ItemStackBuilder(Material.SKULL_ITEM, 1, (short) 3).withHeadOwner(member.getUser().getNickname()).withName(memberElement.name.replace("{PLAYER_NAME}", player.getName())).withLore(memberElement.lore).build(), event -> {
//
//          }));
//        }
//      }
//    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
