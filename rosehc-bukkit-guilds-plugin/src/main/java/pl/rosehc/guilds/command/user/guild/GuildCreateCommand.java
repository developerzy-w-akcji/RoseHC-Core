package pl.rosehc.guilds.command.user.guild;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildCreatePacket;
import pl.rosehc.controller.packet.platform.PlatformAlertMessagePacket;
import pl.rosehc.controller.wrapper.guild.GuildTypeWrapper;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper.GuildItemWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildHelper;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildRegion;
import pl.rosehc.guilds.guild.group.GuildGroup;
import pl.rosehc.guilds.inventories.GuildTypeSelectionInventory;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.helper.SectorHelper;
import pl.rosehc.sectors.sector.SectorType;

public final class GuildCreateCommand {

  private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");
  private final GuildsPlugin plugin;

  public GuildCreateCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild create", "g create", "g zaloz",
      "g stworz"}, description = "Tworzy nową gildię dla podanego gracz   a.")
  public void handleGuildCreate(final @Sender Player player, final @Name("tag") String tag,
      final @Name("name") String name) {
    // TODO: CHECK IF GUILDS ARE DISABLED
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    if (user.getGuild() != null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateYouAlreadyHaveAGuild));
    }

    if (SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getType()
        != SectorType.GAME) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateGuildCanBeOnlyCreatedOnGameSector));
    }

    if (tag.length() < this.plugin.getGuildsConfiguration().pluginWrapper.minTagLength) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateTagIsTooSmall.replace(
              "{MIN_TAG_LENGTH}",
              String.valueOf(this.plugin.getGuildsConfiguration().pluginWrapper.minTagLength))));
    }

    if (tag.length() > this.plugin.getGuildsConfiguration().pluginWrapper.maxTagLength) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateTagIsTooBig.replace(
              "{MAX_TAG_LENGTH}",
              String.valueOf(this.plugin.getGuildsConfiguration().pluginWrapper.maxTagLength))));
    }

    if (name.length() < this.plugin.getGuildsConfiguration().pluginWrapper.minNameLength) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNameIsTooSmall.replace(
              "{MIN_NAME_LENGTH}",
              String.valueOf(this.plugin.getGuildsConfiguration().pluginWrapper.minNameLength))));
    }

    if (name.length() > this.plugin.getGuildsConfiguration().pluginWrapper.maxNameLength) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNameIsTooBig.replace(
              "{MAX_NAME_LENGTH}",
              String.valueOf(this.plugin.getGuildsConfiguration().pluginWrapper.maxNameLength))));
    }

    if (name.equals(tag)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNameAndTagCannotBeTheSame));
    }

    if (!ALPHANUMERIC_PATTERN.matcher(tag).matches()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateTagIsNotAlphanumeric));
    }

    if (!ALPHANUMERIC_PATTERN.matcher(name).matches()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNameIsNotAlphanumeric));
    }

    final GuildTypeSelectionInventory inventory = new GuildTypeSelectionInventory(player, type -> {
      player.closeInventory();
      if (this.plugin.getGuildFactory().findGuildByCredential(tag, true).isPresent()) {
        ChatHelper.sendMessage(player,
            this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateThatGuildTagAlreadyExists);
        return;
      }

      if (this.plugin.getGuildFactory().findGuildByCredential(name).isPresent()) {
        ChatHelper.sendMessage(player,
            this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateThatGuildNameAlreadyExists);
        return;
      }

      if (SectorHelper.getDistanceToNearestSector(player.getLocation())
          <= this.plugin.getGuildsConfiguration().pluginWrapper.minSectorDistance) {
        ChatHelper.sendMessage(player,
            this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateGuildCannotBeCreatedNearBorder.replace(
                "{DISTANCE}", String.valueOf(
                    this.plugin.getGuildsConfiguration().pluginWrapper.minSectorDistance)));
        return;
      }

      if (this.plugin.getGuildFactory().findGuildInside(player.getLocation()).isPresent()
          || this.plugin.getGuildFactory().findGuildNear(player.getLocation()).isPresent()) {
        ChatHelper.sendMessage(player,
            this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateGuildCannotBeCreatedNearGuild);
        return;
      }

      final List<GuildItemWrapper> itemWrapperList = this.plugin.getGuildsConfiguration().pluginWrapper.guildItemWrapperMap.get(
          GuildTypeWrapper.fromOriginal(type));
      final List<Pair<ItemStack, Integer>> toRemoveList = new ArrayList<>();
      final List<Triple<String, Integer, Integer>> notFoundList = new ArrayList<>();
      final double itemsPercentageChange = this.plugin.getGuildsConfiguration().pluginWrapper.guildItemPercentageChangeMap.entrySet()
          .stream().filter(entry -> player.hasPermission(entry.getKey())).findFirst()
          .map(Entry::getValue).orElse(-1D);
      if (!player.hasPermission("guilds-guild-items-bypass")) {
        for (final GuildItemWrapper wrapper : itemWrapperList) {
          final ItemStack itemStack = wrapper.asItemStack();
          int requiredAmount = itemStack.getAmount();
          if (itemsPercentageChange != -1D) {
            requiredAmount -= (int) (requiredAmount * (itemsPercentageChange / 100D));
          }

          final int countedAmount = ItemHelper.countItemAmount(player, itemStack);
          if (countedAmount < requiredAmount) {
            notFoundList.add(Triple.of(
                itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
                    ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name(),
                countedAmount, requiredAmount));
            continue;
          }

          toRemoveList.add(Pair.of(itemStack, requiredAmount));
        }
      }

      if (!notFoundList.isEmpty()) {
        final StringBuilder builder = new StringBuilder(
            this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNoItemsStart + "\n");
        for (final Triple<String, Integer, Integer> pair : notFoundList) {
          builder.append(
              this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNoItemsFormat.replace(
                      "{ITEM_NAME}", pair.getLeft())
                  .replace("{COUNTED_AMOUNT}", String.valueOf(pair.getMiddle()))
                  .replace("{REQUIRED_AMOUNT}", String.valueOf(pair.getRight())));
          builder.append('\n');
        }

        builder.append(this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateNoItemsEnd);
        ChatHelper.sendMessage(player, builder.toString());
        return;
      }

      for (final Pair<ItemStack, Integer> pair : toRemoveList) {
        ItemHelper.removeItem(player, pair.getKey(), pair.getValue());
      }

      final Location centerLocation = player.getLocation().clone();
      centerLocation.setY(this.plugin.getGuildsConfiguration().pluginWrapper.guildY);
      final long parsedStartGuildValidityTime = this.plugin.getGuildsConfiguration().pluginWrapper.parsedStartGuildValidityTime;
      final long parsedStartGuildProtectionTime = this.plugin.getGuildsConfiguration().pluginWrapper.parsedStartGuildProtectionTime;
      final Map<UUID, GuildGroup> defaultGuildGroupMap = GuildHelper.createDefaultGuildGroups();
      final Guild guild = new Guild(name, tag, defaultGuildGroupMap,
          new GuildMember(player.getUniqueId(), user, new HashSet<>(), defaultGuildGroupMap.get(
              this.plugin.getGuildGroupFactory().getLeaderGuildGroup().getUniqueId())), type,
          new GuildRegion(centerLocation,
              this.plugin.getGuildsConfiguration().pluginWrapper.startGuildSize),
          SectorsPlugin.getInstance().getSectorFactory().getCurrentSector(), player.getLocation(),
          System.currentTimeMillis() + parsedStartGuildValidityTime,
          parsedStartGuildProtectionTime != 0L ? System.currentTimeMillis()
              + parsedStartGuildProtectionTime : 0L,
          this.plugin.getGuildsConfiguration().pluginWrapper.startGuildLives);
      guild.startScanningPistons();
      this.plugin.getGuildFactory().registerGuild(guild);
      this.plugin.getSchematicFactory().pasteGuildSchematic(centerLocation);
      this.plugin.getRedisAdapter().sendPacket(
          new GuildCreatePacket(guild.getName(), guild.getTag(), guild.getGuildMembers()[0].wrap(),
              GuildTypeWrapper.fromOriginal(guild.getGuildType()), guild.getGuildRegion().wrap(),
              guild.getCreationSector().getName(),
              SerializeHelper.serializeLocation(guild.getHomeLocation()), guild.getValidityTime(),
              guild.getProtectionTime(), guild.getLives()), "rhc_master_controller", "rhc_guilds");
      this.plugin.getRedisAdapter().sendPacket(new PlatformAlertMessagePacket(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildCreateSuccessBroadcastMessage.replace(
                  "{NAME}", guild.getName()).replace("{TAG}", guild.getTag())
              .replace("{PLAYER_NAME}", player.getName()), false), "rhc_platform");
      this.plugin.getServer().getScheduler()
          .scheduleSyncDelayedTask(this.plugin, () -> player.teleport(centerLocation), 2L);
    });
    inventory.open();
  }
}
