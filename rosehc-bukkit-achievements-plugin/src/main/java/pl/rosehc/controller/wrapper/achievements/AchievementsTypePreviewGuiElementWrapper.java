package pl.rosehc.controller.wrapper.achievements;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiEnchantmentWrapper;
import pl.rosehc.platform.PlatformConfiguration.CustomItemType;

public final class AchievementsTypePreviewGuiElementWrapper extends DefaultSpigotGuiElementWrapper {

  @Override
  public ItemStack asItemStack() {
    ItemStackBuilder builder;
    try {
      final CustomItemType itemType = CustomItemType.valueOf(this.material);
      final ItemStack itemStack = itemType.getResolver().get();
      itemStack.setAmount(this.amount);
      builder = new ItemStackBuilder(itemStack);
    } catch (final Exception ignored) {
      builder = new ItemStackBuilder(Material.matchMaterial(this.material), this.amount, this.data);
    }

    if (this.enchantments != null && !this.enchantments.isEmpty()) {
      final Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
      for (final SpigotGuiEnchantmentWrapper enchantment : this.enchantments) {
        enchantmentMap.put(Enchantment.getByName(enchantment.enchantmentName),
            enchantment.enchantmentLevel);
      }

      builder.withEnchantments(enchantmentMap);
    }

    if (this.name != null) {
      builder.withName(this.name);
    }
    if (this.lore != null && !this.lore.isEmpty()) {
      builder.withLore(this.lore);
    }

    return builder.build();
  }
}
