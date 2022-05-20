package pl.rosehc.controller.wrapper.achievements;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiEnchantmentWrapper;

public final class AchievementsUserProfileAllStatisticsGuiElementWrapper extends
    DefaultSpigotGuiElementWrapper {

  public ItemStack asItemStack(final GameProfile playerProfile) {
    final ItemStackBuilder builder =
        !this.material.equals("CURRENT_PLAYER_HEAD") ? new ItemStackBuilder(
            Material.matchMaterial(this.material), this.amount, this.data)
            : new ItemStackBuilder(Material.SKULL_ITEM, 1, (short) 3);
    if (this.material.equals("CURRENT_PLAYER_HEAD")) {
      builder.withHeadOwner(playerProfile);
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
