package pl.rosehc.guilds.listener.entity;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.rosehc.adapter.helper.LocationHelper;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper.MaterialDataWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildRegenerationBlockState;
import pl.rosehc.sectors.SectorsPlugin;

public final class EntityExplodeListener implements Listener {

  private final GuildsPlugin plugin;

  public EntityExplodeListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onExplode(final EntityExplodeEvent event) {
    final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    if (hour <= 17 || hour >= 21) {
      event.setCancelled(true);
      return;
    }

    final Location location = event.getLocation();
    if (location.getY() >= 50D) {
      event.setCancelled(true);
      return;
    }

    final Guild guild = this.plugin.getGuildFactory().findGuildInside(location)
        .orElseGet(() -> this.getGuildFromBlockList(event));
    if (guild == null) {
      event.setCancelled(true);
      return;
    }

    if (guild.getGuildRegion().isInsideCenter(location)) {
      event.setCancelled(true);
      return;
    }

    if (guild.getProtectionTime() > System.currentTimeMillis()) {
      event.setCancelled(true);
      return;
    }

    final boolean canSendNotification = guild.canSendTntExplosionNotification();
    guild.updateTntExplosionTime();
    guild.updateTntExplosionNotificationTime();
    if (canSendNotification) {
      guild.broadcastChatMessage(
          this.plugin.getGuildsConfiguration().messagesWrapper.tntHasJustExplodedOnYourTerrain);
    }

    final boolean canAddRegenerationBlock = SectorsPlugin.getInstance().getSectorFactory()
        .getCurrentSector().equals(guild.getCreationSector());
    final PluginWrapper pluginWrapper = this.plugin.getGuildsConfiguration().pluginWrapper;
    if (canAddRegenerationBlock) {
      for (final Block block : event.blockList()) {
        Material type = block.getType();
        byte data = block.getData();
        final MaterialDataWrapper replacement = pluginWrapper.regenerationBlockReplacementMap.get(
            PluginWrapper.createMaterialDataWrapper(type.name(), data));
        if (replacement != null) {
          type = Material.matchMaterial(replacement.material);
          data = replacement.data;
        }

        guild.addRegenerationBlock(
            new GuildRegenerationBlockState(type, data, block.getX(), block.getY(), block.getZ()));
      }
    }

    final List<Location> sphereBlockList = LocationHelper.sphere(location, 4, 4, false, true);
    for (final Location sphereBlockLocation : sphereBlockList) {
      final Block block = sphereBlockLocation.getBlock();
      Material type = block.getType();
      byte data = block.getData();
      final Double chance = pluginWrapper.explosionChanceMap.get(
          PluginWrapper.createMaterialDataWrapper(type.name(), data).toString());
      if (chance == null) {
        continue;
      }

      if (chance >= 100D || chance > ThreadLocalRandom.current().nextDouble(0D, 100D)) {
        final MaterialDataWrapper replacement = pluginWrapper.regenerationBlockReplacementMap.get(
            PluginWrapper.createMaterialDataWrapper(type.name(), data).toString());
        if (replacement != null) {
          type = Material.matchMaterial(replacement.material);
          data = replacement.data;
        }

        block.setType(Material.AIR);
        guild.addRegenerationBlock(
            new GuildRegenerationBlockState(type, data, block.getX(), block.getY(), block.getZ()));
      }
    }
  }

  private Guild getGuildFromBlockList(final EntityExplodeEvent event) {
    final List<Block> blockList = event.blockList();
    for (final Block block : blockList) {
      final Optional<Guild> guildOptional = this.plugin.getGuildFactory()
          .findGuildInside(block.getLocation());
      if (guildOptional.isPresent()) {
        return guildOptional.get();
      }
    }

    return null;
  }
}
