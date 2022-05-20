package pl.rosehc.achievements.achievement.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.Achievement;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.achievements.user.AchievementsUser;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.StringHelper;
import pl.rosehc.adapter.inventory.BukkitInventory;
import pl.rosehc.adapter.inventory.BukkitInventoryElement;
import pl.rosehc.controller.packet.platform.user.PlatformUserMessagePacket;
import pl.rosehc.controller.wrapper.achievements.AchievementsAchievementPreviewGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiElementWrapper;
import pl.rosehc.controller.wrapper.spigot.SpigotGuiWrapper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserChatSettings;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class AchievementTypeInventory {

  private final Player player;
  private final BukkitInventory inventory;

  public AchievementTypeInventory(final Player player, final AchievementType type) {
    this.player = player;
    final SpigotGuiWrapper mainGuiWrapper = AchievementsPlugin.getInstance()
        .getAchievementsConfiguration().inventoryMap.get(type.name().toLowerCase());
    if (Objects.isNull(mainGuiWrapper)) {
      ChatHelper.sendMessage(player,
          PlatformPlugin.getInstance().getPlatformConfiguration().messagesWrapper.guiNotFound);
      throw new IllegalStateException("Achievement type (" + type.name() + ") gui not configured.");
    }

    final List<Achievement> achievementList = AchievementsPlugin.getInstance()
        .getAchievementFactory().findAchievementListByType(type);
    if (achievementList.isEmpty()) {
      throw new IllegalStateException("Achievements by type " + type.name() + " were not found!");
    }

    final AchievementsUser user = AchievementsPlugin.getInstance().getAchievementsUserFactory()
        .findUserByPlayer(player).orElseThrow(() -> {
          ChatHelper.sendMessage(player, PlatformPlugin.getInstance()
              .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                  player.getName()));
          return new IllegalStateException("User not found.");
        });
    this.inventory = new BukkitInventory(mainGuiWrapper.inventoryName,
        mainGuiWrapper.inventorySize);
    final SpigotGuiElementWrapper fillElement = mainGuiWrapper.fillElement;
    if (fillElement != null) {
      this.inventory.fillWith(fillElement.asItemStack());
    }

    for (final Entry<String, SpigotGuiElementWrapper> entry : mainGuiWrapper.elements.entrySet()) {
      final SpigotGuiElementWrapper element = entry.getValue();
      if (!(element instanceof AchievementsAchievementPreviewGuiElementWrapper) && !entry.getKey()
          .equalsIgnoreCase("back")) {
        this.inventory.setElement(element.slot, new BukkitInventoryElement(element.asItemStack()));
      }
    }

    for (final Achievement achievement : achievementList) {
      final SpigotGuiElementWrapper element = mainGuiWrapper.elements.get(
          "level" + achievement.getLevel());
      if (!(element instanceof AchievementsAchievementPreviewGuiElementWrapper)) {
        continue;
      }

      final AchievementsAchievementPreviewGuiElementWrapper achievementPreviewElement = (AchievementsAchievementPreviewGuiElementWrapper) element;
      final Function<Double, String> formatFunction = type.getFormatFunction();
      final double maxStatistic = achievement.getRequiredStatistics(), statistic = Math.min(
          user.getAchievementStatistic(type), maxStatistic);
      this.inventory.setElement(element.slot, new BukkitInventoryElement(
          new ItemStackBuilder(Material.matchMaterial(element.material), 1,
              achievementPreviewElement.data).withName(
              achievementPreviewElement.name).withLore(achievementPreviewElement.lore.stream().map(
                  content -> content.replace("{PROGRESS_BAR}", StringHelper.getProgressBar(
                          (int) (statistic - achievement.getRequiredStatistics()), 0, 25, ':'))
                      .replace("{CURRENT_STATISTIC}", formatFunction.apply(statistic))
                      .replace("{MAX_STATISTIC}", formatFunction.apply(maxStatistic))
                      .replace("{RECEIVED}",
                          user.hasCompletedAchievement(achievement) ? "&ctak" : "&cnie"))
              .collect(Collectors.toList())).build(), event -> {
        if (statistic >= maxStatistic && user.completeAchievement(achievement)) {
          achievement.getRewardList().forEach(reward -> {
            reward.give(this.player);
            reward.give(user);
          });
          Bukkit.getScheduler().runTaskAsynchronously(AchievementsPlugin.getInstance(), () -> {
            final List<UUID> uuidList = new ArrayList<>();
            for (final SectorUser targetUser : SectorsPlugin.getInstance().getSectorUserFactory()
                .getUserMap().values()) {
              if (PlatformPlugin.getInstance().getPlatformUserFactory()
                  .findUserByUniqueId(targetUser.getUniqueId()).map(PlatformUser::getChatSettings)
                  .filter(PlatformUserChatSettings::isAchievements).isPresent()) {
                uuidList.add(targetUser.getUniqueId());
              }
            }

            if (!uuidList.isEmpty()) {
              PlatformPlugin.getInstance().getRedisAdapter().sendPacket(
                  new PlatformUserMessagePacket(uuidList, AchievementsPlugin.getInstance()
                      .getAchievementsConfiguration().rewardReceiveMessageMap.getOrDefault(
                          type.name() + ":" + achievement.getLevel(),
                          "No message by type " + type.name() + " and " + achievement.getLevel()
                              + " was found.").replace("{PLAYER_NAME}", player.getName())),
                  "rhc_platform");
            }
          });
        }
      }));
    }

    final SpigotGuiElementWrapper backElement = mainGuiWrapper.elements.get("back");
    if (Objects.nonNull(backElement)) {
      this.inventory.setElement(backElement.slot,
          new BukkitInventoryElement(backElement.asItemStack(),
              event -> player.performCommand("achievements")));
    }
  }

  public void open() {
    if (this.player.isOnline()) {
      this.inventory.openInventory(this.player);
    }
  }
}
