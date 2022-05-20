package pl.rosehc.guilds.listener.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.builder.ItemStackBuilder;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.controller.packet.guild.user.GuildUserCacheVictimPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserClearFightersPacket;
import pl.rosehc.controller.packet.guild.user.GuildUserSynchronizeRankingPacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserMessagePacket;
import pl.rosehc.guilds.GuildsConfiguration.MessagesWrapper;
import pl.rosehc.guilds.GuildsConfiguration.MessagesWrapper.TitleMessageWrapper;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.ranking.RankingPointsCalculationHelper;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.guilds.user.GuildUserRanking;
import pl.rosehc.guilds.user.event.GuildUserUpdateDeathsEvent;
import pl.rosehc.guilds.user.event.GuildUserUpdateKillStreakEvent;
import pl.rosehc.guilds.user.event.GuildUserUpdateKillsEvent;
import pl.rosehc.guilds.user.event.GuildUserUpdatePointsEvent;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.platform.user.subdata.PlatformUserChatSettings;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class PlayerDeathListener implements Listener {

  private static final Predicate<PlatformUserChatSettings> IS_DEATHS_MESSAGES_ENABLED_PREDICATE = PlatformUserChatSettings::isDeaths;
  private static final Predicate<PlatformUserChatSettings> IS_KILLS_MESSAGES_ENABLED_PREDICATE = PlatformUserChatSettings::isKills;
  private final GuildsPlugin plugin;

  public PlayerDeathListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  private static String formatKillMessage(final MessagesWrapper messagesWrapper,
      final GuildUser killerUser, final GuildUser victimUser, final int victimPointsChange,
      final int killerPointsChange) {
    final Guild killerUserGuild = killerUser.getGuild(), victimUserGuild = victimUser.getGuild();
    return messagesWrapper.killInfoMessageMap.getOrDefault(
            Objects.nonNull(killerUserGuild) && Objects.nonNull(victimUserGuild)
                ? "kill_info_message_with_both_guild"
                : Objects.nonNull(killerUserGuild) ? "kill_info_message_with_killer_guild"
                    : Objects.nonNull(victimUserGuild) ? "kill_info_message_with_victim_guild"
                        : "kill_info_message_without_guild", "kill_info_message_without_guild")
        .replace("{VICTIM_PLAYER_NAME}", victimUser.getNickname())
        .replace("{VICTIM_POINTS_CHANGE}", String.valueOf(victimPointsChange))
        .replace("{KILLER_PLAYER_NAME}", killerUser.getNickname())
        .replace("{KILLER_POINTS_CHANGE}", String.valueOf(killerPointsChange))
        .replace("{VICTIM_GUILD_TAG}",
            Objects.nonNull(victimUserGuild) ? victimUserGuild.getTag() : "")
        .replace("{KILLER_GUILD_TAG}",
            Objects.nonNull(killerUserGuild) ? killerUserGuild.getTag() : "");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDeath(final PlayerDeathEvent event) {
    final Player victim = event.getEntity();
    event.setDeathMessage(null);
    this.plugin.getGuildUserFactory().findUserByUniqueId(victim.getUniqueId())
        .ifPresent(victimUser -> {
          final GuildUserRanking victimUserRanking = victimUser.getUserRanking();
          final PluginWrapper pluginWrapper = this.plugin.getGuildsConfiguration().pluginWrapper;
          final MessagesWrapper messagesWrapper = this.plugin.getGuildsConfiguration().messagesWrapper;
          try {
            victimUserRanking.setDeaths(victimUserRanking.getDeaths() + 1);
            victimUserRanking.setKillStreak(0);
            Player killer = victim.getKiller();
            this.plugin.getServer().getPluginManager().callEvent(
                new GuildUserUpdateDeathsEvent(victimUser, victimUserRanking.getDeaths()));
            this.plugin.getServer().getPluginManager().callEvent(
                new GuildUserUpdateKillStreakEvent(victimUser, victimUserRanking.getKillStreak()));
            if (Objects.isNull(killer)) {
              killer = victimUser.findLastOptionalFighter().filter(info ->
                      info.getFightTime() + pluginWrapper.parsedVictimKillerConsiderationTimeoutTime
                          > System.currentTimeMillis())
                  .map(info -> this.plugin.getServer().getPlayer(info.getUniqueId())).orElse(null);
              if (Objects.isNull(killer)) {
                victimUser.clearFighters();
                victimUserRanking.setPoints(victimUserRanking.getPoints() - 1);
                this.plugin.getServer().getPluginManager().callEvent(
                    new GuildUserUpdatePointsEvent(victimUser, true,
                        victimUserRanking.getPoints()));
                this.plugin.getRedisAdapter()
                    .sendPacket(new GuildUserClearFightersPacket(victimUser.getUniqueId()),
                        "rhc_master_controller", "rhc_guilds");
                this.sendDeathMessageToUsers(IS_DEATHS_MESSAGES_ENABLED_PREDICATE,
                    messagesWrapper.deathInfo.replace("{PLAYER_NAME}", victim.getName()));
                return;
              }
            }

            final List<ItemStack> itemStackList = new ArrayList<>(event.getDrops());
            event.getDrops().clear();
            ItemHelper.addItems(killer, itemStackList);
            ItemHelper.addItem(killer, new ItemStackBuilder(
                new ItemStack(Material.SKULL_ITEM, 1, (short) 3)).withHeadOwner(
                ((CraftPlayer) victim).getHandle().getProfile()).build());
            killer.getWorld().strikeLightningEffect(victim.getLocation());
            if (killer.equals(victim)) {
              victimUser.clearFighters();
              this.plugin.getRedisAdapter()
                  .sendPacket(new GuildUserClearFightersPacket(victimUser.getUniqueId()),
                      "rhc_master_controller", "rhc_guilds");
              this.sendDeathMessageToUsers(IS_DEATHS_MESSAGES_ENABLED_PREDICATE,
                  messagesWrapper.deathInfo.replace("{PLAYER_NAME}", victim.getName()));
              return;
            }

            final Player finalKiller = killer;
            this.plugin.getGuildUserFactory().findUserByUniqueId(killer.getUniqueId())
                .ifPresent(killerUser -> {
                  final long victimKillerLastKillTimeoutTime = pluginWrapper.parsedVictimKillerLastKillTimeoutTime;
                  if (victimKillerLastKillTimeoutTime != 0L) {
                    final long attackerTime = killerUser.getAttackerTime(
                        victimUser), victimTime = victimUser.getVictimTime(killerUser);
                    final boolean isLastAttacker = attackerTime != 0L
                        && attackerTime > System.currentTimeMillis(), isLastVictim =
                        victimTime != 0L && victimTime > System.currentTimeMillis();
                    if (isLastAttacker || isLastVictim) {
                      ChatHelper.sendMessage(victim,
                          isLastAttacker ? messagesWrapper.cannotBeKilledByLastKillerToVictim
                              : messagesWrapper.cannotKillLastVictimToVictim);
                      ChatHelper.sendMessage(finalKiller,
                          isLastAttacker ? messagesWrapper.cannotBeKilledByLastKillerToKiller
                              : messagesWrapper.cannotKillLastVictimToKiller);
                      victimUser.clearFighters();
                      this.plugin.getRedisAdapter()
                          .sendPacket(new GuildUserClearFightersPacket(victimUser.getUniqueId()),
                              "rhc_master_controller", "rhc_guilds");
                      return;
                    }
                  }

                  if (Objects.nonNull(finalKiller.getAddress()) && Objects.nonNull(
                      victim.getAddress()) && finalKiller.getAddress().getHostString()
                      .equals(victim.getAddress().getHostString())) {
                    ChatHelper.sendMessage(victim, messagesWrapper.foundMultiAccountToVictim);
                    ChatHelper.sendMessage(finalKiller, messagesWrapper.foundMultiAccountToKiller);
                    victimUser.clearFighters();
                    this.plugin.getRedisAdapter()
                        .sendPacket(new GuildUserClearFightersPacket(victimUser.getUniqueId()),
                            "rhc_master_controller", "rhc_guilds");
                    return;
                  }

                  final GuildUserRanking killerUserRanking = killerUser.getUserRanking();
                  final Pair<Integer, Integer> calculatedPoints = RankingPointsCalculationHelper.calculatePoints(
                      pluginWrapper, victimUserRanking.getPoints(),
                      killerUserRanking.getPoints());
                  final long victimCacheTime =
                      System.currentTimeMillis() + victimKillerLastKillTimeoutTime;
                  killerUserRanking.setPoints(
                      killerUserRanking.getPoints() + calculatedPoints.getLeft());
                  killerUserRanking.setKills(killerUserRanking.getKills() + 1);
                  killerUserRanking.setKillStreak(killerUserRanking.getKillStreak() + 1);
                  victimUserRanking.setPoints(
                      victimUserRanking.getPoints() - calculatedPoints.getRight());
                  victimUser.clearFighters();
                  killerUser.cacheVictim(victimUser.getUniqueId(), victimCacheTime);
                  this.plugin.getServer().getPluginManager().callEvent(
                      new GuildUserUpdateKillStreakEvent(killerUser,
                          killerUserRanking.getKillStreak()));
                  this.plugin.getServer().getPluginManager().callEvent(
                      new GuildUserUpdateKillsEvent(killerUser, killerUserRanking.getKills()));
                  this.plugin.getServer().getPluginManager().callEvent(
                      new GuildUserUpdatePointsEvent(killerUser, false,
                          killerUserRanking.getPoints()));
                  this.plugin.getServer().getPluginManager().callEvent(
                      new GuildUserUpdatePointsEvent(victimUser, true,
                          victimUserRanking.getPoints()));
                  this.plugin.getRedisAdapter().sendPacket(
                      new GuildUserCacheVictimPacket(killerUser.getUniqueId(),
                          victimUser.getUniqueId(), victimCacheTime), "rhc_master_controller",
                      "rhc_platform");
                  this.plugin.getRedisAdapter()
                      .sendPacket(new GuildUserClearFightersPacket(victimUser.getUniqueId()),
                          "rhc_master_controller", "rhc_guilds");
                  this.plugin.getRedisAdapter().sendPacket(
                      new GuildUserSynchronizeRankingPacket(killerUser.getUniqueId(),
                          killerUserRanking.getPoints(), killerUserRanking.getKills(),
                          killerUserRanking.getDeaths(), killerUserRanking.getKillStreak()),
                      "rhc_master_controller", "rhc_guilds");
                  final TitleMessageWrapper killTitle = victimUser.getGuild() == null
                      ? this.plugin.getGuildsConfiguration().messagesWrapper.killTitleWithoutGuild
                      : this.plugin.getGuildsConfiguration().messagesWrapper.killTitleWithGuild;
                  ChatHelper.sendTitle(finalKiller, killTitle.title,
                      killTitle.subTitle.replace("{TAG}",
                              victimUser.getGuild() != null ? victimUser.getGuild().getTag() : "")
                          .replace("{POINTS}", String.valueOf(calculatedPoints.getLeft()))
                          .replace("{PLAYER_NAME}", victimUser.getNickname()));
                  this.sendDeathMessageToUsers(IS_KILLS_MESSAGES_ENABLED_PREDICATE,
                      formatKillMessage(messagesWrapper, killerUser, victimUser,
                          calculatedPoints.getRight(), calculatedPoints.getLeft()));
                });
          } finally {
            this.plugin.getRedisAdapter().sendPacket(
                new GuildUserSynchronizeRankingPacket(victimUser.getUniqueId(),
                    victimUserRanking.getPoints(), victimUserRanking.getKills(),
                    victimUserRanking.getDeaths(), victimUserRanking.getKillStreak()),
                "rhc_master_controller", "rhc_guilds");
          }
        });
  }

  private void sendDeathMessageToUsers(
      final Predicate<PlatformUserChatSettings> chatSettingPredicate, final String message) {
    this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
      final List<UUID> uuidList = new ArrayList<>();
      for (final SectorUser user : SectorsPlugin.getInstance().getSectorUserFactory().getUserMap()
          .values()) {
        if (PlatformPlugin.getInstance().getPlatformUserFactory()
            .findUserByUniqueId(user.getUniqueId()).map(PlatformUser::getChatSettings)
            .filter(chatSettingPredicate).isPresent()) {
          uuidList.add(user.getUniqueId());
        }
      }

      if (!uuidList.isEmpty()) {
        this.plugin.getRedisAdapter()
            .sendPacket(new PlatformUserMessagePacket(uuidList, message), "rhc_platform");
      }
    });
  }
}
