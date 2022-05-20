package pl.rosehc.achievements.achievement.inventory;

import java.util.Map.Entry;
import java.util.Objects;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.achievements.AchievementsTypePreviewGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.platform.PlatformPlugin;

public final class AchievementMainInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public AchievementMainInventory(final Player player) {
    this.player = player;
    final SpigotGuiWrapper mainGuiWrapper = AchievementsPlugin.getInstance()
        .getAchievementsConfiguration().inventoryMap.get("main");
    if (Objects.isNull(mainGuiWrapper)) {
      throw new BladeExitMessage(ChatHelper.colored(
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound));
    }

    this.inventory = new BukkitInventory(mainGuiWrapper.inventoryName,
        mainGuiWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = mainGuiWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : mainGuiWrapper.elements.entrySet()) {
      final SpigotGuiElementWrapper element = entry.getValue();
      if (!(element instanceof AchievementsTypePreviewGuiElementWrapper)) {
        this.inventory.setElement(element.slot, new BukkitInventoryElement(element.asItemStack()));
      }
    }

    for (final AchievementType type : AchievementType.values()) {
      final SpigotGuiElementWrapper element = mainGuiWrapper.elements.get(
          type.name().toLowerCase());
      if (!(element instanceof AchievementsTypePreviewGuiElementWrapper)) {
        continue;
      }

      final AchievementsTypePreviewGuiElementWrapper typePreviewElement = (AchievementsTypePreviewGuiElementWrapper) element;
      this.inventory.setElement(typePreviewElement.slot,
          new BukkitInventoryElement(typePreviewElement.asItemStack(), event -> {
            final AchievementTypeInventory typeInventory = new AchievementTypeInventory(player,
                type);
            typeInventory.open();
          }));
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
