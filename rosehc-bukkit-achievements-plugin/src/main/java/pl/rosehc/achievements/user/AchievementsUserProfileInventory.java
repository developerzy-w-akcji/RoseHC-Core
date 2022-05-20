package pl.rosehc.achievements.user;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.AchievementBoostType;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.wrapper.achievements.AchievementsAchievementPreviewGuiElementWrapper;
import pl.rosehc.controller.wrapper.achievements.AchievementsUserProfileAllStatisticsGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.DefaultSpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;

public final class AchievementsUserProfileInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public AchievementsUserProfileInventory(final Player player, final AchievementsUser user) {
    this.player = player;
    final SpigotGuiWrapper userProfileGuiWrapper = AchievementsPlugin.getInstance()
        .getAchievementsConfiguration().inventoryMap.get("user_profile");
    if (Objects.isNull(userProfileGuiWrapper)) {
      ChatHelper.sendMessage(player,
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound);
      throw new IllegalStateException("Achievement user profile gui not configured.");
    }

    this.inventory = new BukkitInventory(userProfileGuiWrapper.inventoryName,
        userProfileGuiWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = userProfileGuiWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : userProfileGuiWrapper.elements.entrySet()) {
      final SpigotGuiElementWrapper element = entry.getValue();
      if (!(element instanceof AchievementsAchievementPreviewGuiElementWrapper) && !entry.getKey()
          .equalsIgnoreCase("all_statistics") && !entry.getKey().equalsIgnoreCase("all_boosts")
          && !entry.getKey().equalsIgnoreCase("current_guild")) {
        this.inventory.setElement(element.slot, new BukkitInventoryElement(element.asItemStack()));
      }
    }

    this.setAllStatisticsItem(user, userProfileGuiWrapper);
    this.setCurrentGuildItem(user, userProfileGuiWrapper);
    this.setAllBoostsItem(user, userProfileGuiWrapper);
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }

  private void setAllStatisticsItem(final AchievementsUser user,
      final SpigotGuiWrapper userProfileGuiWrapper) {
    final SpigotGuiElementWrapper element = userProfileGuiWrapper.elements.get("all_statistics");
    if (!(element instanceof AchievementsUserProfileAllStatisticsGuiElementWrapper)) {
      return;
    }

    final AchievementsUserProfileAllStatisticsGuiElementWrapper allStatisticsElement = (AchievementsUserProfileAllStatisticsGuiElementWrapper) element;
    final AtomicReference<String> kdr = new AtomicReference<>("N/A"), kills = new AtomicReference<>(
        "N/A"), deaths = new AtomicReference<>("N/A"), killStreak = new AtomicReference<>("N/A");
    try {
      Class.forName("pl.rosehc.guilds.GuildsPlugin");
      final GuildUser guildUser = GuildsPlugin.getInstance().getGuildUserFactory()
          .findUserByUniqueId(user.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
              ChatHelper.colored(PlatformPlugin.getInstance()
                  .getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                      "{PLAYER_NAME}", user.getNickname()))));
      kills.set(String.valueOf(guildUser.getUserRanking().getKills()));
      deaths.set(String.valueOf(guildUser.getUserRanking().getKills()));
      killStreak.set(String.valueOf(guildUser.getUserRanking().getKills()));
      kdr.set(String.format("%.2f", guildUser.getUserRanking().getKDR()));
    } catch (final Exception ignored) {
    }

    final PlatformUser platformUser = PlatformPlugin.getInstance().getPlatformUserFactory()
        .findUserByNickname(user.getNickname()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    user.getNickname()))));
    final String spendTime = TimeHelper.timeToString((long) user.getAchievementStatistic(
        AchievementType.SPEND_TIME)), miningLevel = String.valueOf(
        platformUser.getDropSettings().getLevel());
    this.inventory.setElement(allStatisticsElement.slot, new BukkitInventoryElement(
        new ItemStackBuilder(allStatisticsElement.asItemStack(
            ((CraftPlayer) this.player).getHandle().getProfile())).withLore(
            allStatisticsElement.lore.stream().map(
                    content -> content.replace("{KILLS}", kills.get()).replace("{DEATHS}", deaths.get())
                        .replace("{KILL_STREAK}", killStreak.get()).replace("{KDR}", kdr.get())
                        .replace("{SPEND_TIME", spendTime).replace("{MINING_LEVEL}", miningLevel))
                .collect(Collectors.toList())).build()));
  }

  private void setCurrentGuildItem(final AchievementsUser user,
      final SpigotGuiWrapper userProfileGuiWrapper) {
    final SpigotGuiElementWrapper element = userProfileGuiWrapper.elements.get("current_guild");
    if (!(element instanceof DefaultSpigotGuiElementWrapper)) {
      return;
    }

    final DefaultSpigotGuiElementWrapper guildInfoElement = (DefaultSpigotGuiElementWrapper) element;
    final AtomicReference<String> currentGuildName = new AtomicReference<>(
        "N/A"), currentGuildTag = new AtomicReference<>("N/A");
    try {
      Class.forName("pl.rosehc.guilds.GuildsPlugin");
      final Guild guild = GuildsPlugin.getInstance().getGuildUserFactory()
          .findUserByUniqueId(user.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
              ChatHelper.colored(PlatformPlugin.getInstance()
                  .getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                      "{PLAYER_NAME}", user.getNickname())))).getGuild();
      currentGuildName.set(Objects.nonNull(guild) ? guild.getName() : "Brak");
      currentGuildTag.set(Objects.nonNull(guild) ? guild.getTag() : "Brak");
    } catch (final Exception ignored) {
    }

    this.inventory.setElement(guildInfoElement.slot, new BukkitInventoryElement(
        new ItemStackBuilder(guildInfoElement.asItemStack()).withName(
            guildInfoElement.name.replace("{NAME}", currentGuildName.get())
                .replace("{TAG}", currentGuildTag.get())).withLore(guildInfoElement.lore.stream()
            .map(content -> content.replace("{NAME}", currentGuildName.get())
                .replace("{TAG}", currentGuildTag.get())).collect(Collectors.toList())).build()));
  }

  private void setAllBoostsItem(final AchievementsUser user,
      final SpigotGuiWrapper userProfileGuiWrapper) {
    final SpigotGuiElementWrapper element = userProfileGuiWrapper.elements.get("all_boosts");
    if (!(element instanceof DefaultSpigotGuiElementWrapper)) {
      return;
    }

    final DefaultSpigotGuiElementWrapper allBoostsElement = (DefaultSpigotGuiElementWrapper) element;
    final double killAndDeathPointsBoost = user.getAchievementBoost(
        AchievementBoostType.KILL_AND_DEATH_POINTS), killPointsBoost = user.getAchievementBoost(
        AchievementBoostType.KILL_POINTS);
    final double dropChanceBoost = user.getAchievementBoost(AchievementBoostType.DROP_CHANCE);
    final String additionalRankingBoostInfo =
        killAndDeathPointsBoost != -1D || killPointsBoost != -1D ? String.format("%.2f",
            killAndDeathPointsBoost != -1 ? killAndDeathPointsBoost : killPointsBoost) + "%"
            : "Brak!";
    final String additionalDropBoostInfo =
        dropChanceBoost != -1D ? String.format("%.2f", dropChanceBoost) + "%" : "Brak!";
    this.inventory.setElement(allBoostsElement.slot, new BukkitInventoryElement(
        new ItemStackBuilder(allBoostsElement.asItemStack()).withLore(allBoostsElement.lore.stream()
            .map(content -> content.replace("{ADDITIONAL_RANKING}", additionalRankingBoostInfo)
                .replace("{LESS_RANKING_LOSS}",
                    killAndDeathPointsBoost != -1 ? additionalRankingBoostInfo : "Brak!")
                .replace("{ADDITIONAL_DROP_CHANCE}", additionalDropBoostInfo))
            .collect(Collectors.toList())).build()));
  }
}
