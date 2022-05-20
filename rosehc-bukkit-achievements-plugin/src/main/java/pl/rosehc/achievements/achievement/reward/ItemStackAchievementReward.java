package pl.rosehc.achievements.achievement.reward;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.helper.ItemHelper;

public final class ItemStackAchievementReward implements IAchievementReward {

  private final ItemStack itemStack;

  ItemStackAchievementReward(final ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  @Override
  public void give(final Player player) {
    ItemHelper.addItem(player, this.itemStack);
  }
}
