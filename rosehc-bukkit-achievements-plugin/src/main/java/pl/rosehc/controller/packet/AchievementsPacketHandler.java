package pl.rosehc.controller.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.stream.Collectors;
import pl.rosehc.achievements.AchievementsConfiguration;
import pl.rosehc.achievements.AchievementsConfiguration.AchievementWrapper;
import pl.rosehc.achievements.AchievementsPlugin;
import pl.rosehc.achievements.achievement.Achievement;
import pl.rosehc.achievements.achievement.AchievementType;
import pl.rosehc.achievements.achievement.reward.AchievementRewardCreator;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.redis.packet.PacketHandler;
import pl.rosehc.controller.packet.configuration.ConfigurationSynchronizePacket;
import pl.rosehc.sectors.SectorsPlugin;

public final class AchievementsPacketHandler implements ConfigurationSynchronizePacketHandler,
    PacketHandler {

  private final AchievementsPlugin plugin;

  public AchievementsPacketHandler(final AchievementsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handle(final ConfigurationSynchronizePacket packet) {
    if (SectorsPlugin.getInstance().isLoaded() && packet.getConfigurationName().equals(
        "pl.rosehc.controller.configuration.impl.configuration.AchievementsConfiguration")) {
      final AchievementsConfiguration configuration = ConfigurationHelper.deserializeConfiguration(
          packet.getSerializedConfiguration(), AchievementsConfiguration.class);
      this.plugin.setAchievementsConfiguration(configuration);
      final Multimap<AchievementType, Achievement> achievementMultimap = Multimaps.synchronizedMultimap(
          HashMultimap.create());
      for (final AchievementWrapper wrapper : configuration.achievementWrapperList) {
        final AchievementType type = wrapper.achievementTypeWrapper.toOriginal();
        achievementMultimap.put(type, new Achievement(type,
            wrapper.rewardList.stream().map(AchievementRewardCreator::create)
                .collect(Collectors.toList()), wrapper.requiredStatistics, wrapper.level));
      }

      this.plugin.getAchievementFactory().getAchievementMultimap().clear();
    }
  }
}
