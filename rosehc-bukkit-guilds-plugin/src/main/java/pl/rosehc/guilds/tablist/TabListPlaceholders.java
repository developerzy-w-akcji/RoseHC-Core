package pl.rosehc.guilds.tablist;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildRanking;
import pl.rosehc.guilds.ranking.Ranking;
import pl.rosehc.guilds.ranking.RankingIdentifiableType;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.guilds.user.GuildUserRanking;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

@SuppressWarnings("SpellCheckingInspection")
public final class TabListPlaceholders {

  private static final Set<TabListPlaceholder> PLACEHOLDER_SET = new HashSet<>();
  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("kk:mm:ss");
  private static final char TABLIST_PLACEHOLDER_START = '{', TABLIST_PLACE_HOLDER_END = '}';

  static {
    FORMATTER.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Warsaw")));
  }

  private TabListPlaceholders() {
  }

  public static String replace(final Player player, String input) {
    for (final TabListPlaceholder placeholder : PLACEHOLDER_SET) {
      if (input.contains(
          TABLIST_PLACEHOLDER_START + placeholder.getName() + TABLIST_PLACE_HOLDER_END)) {
        input = input.replace(
            TABLIST_PLACEHOLDER_START + placeholder.getName() + TABLIST_PLACE_HOLDER_END,
            placeholder.replace(player));
      }
    }

    return input;
  }

  public static void registerDefaults() {
    // Tops
    for (int position = 0; position < 16; position++) {
      final int fixedPosition = position + 1, normalPosition = position;
      register(new TabListPlaceholder("user_normal_top_" + fixedPosition, top((ranking, user) -> {
        final GuildUser rankingOwner = ((GuildUserRanking) ranking).getOwner();
        return GuildsPlugin.getInstance()
            .getGuildsConfiguration().messagesWrapper.topUserRankFormatMap.get(normalPosition)
            .replace("{PLAYER_NAME}", rankingOwner.getNickname())
            .replace("{POINTS}", String.valueOf(ranking.getPoints())).replace("{ONLINE_COLOR}",
                SectorsPlugin.getInstance().getSectorUserFactory().getUserMap()
                    .containsKey(rankingOwner.getUniqueId()) ? ChatColor.GREEN.toString()
                    : ChatColor.RED.toString());
      }, RankingIdentifiableType.USER_GAME, position)));
      for (final RankingIdentifiableType type : RankingIdentifiableType.values()) {
        if (type.ordinal() >= 1) {
          register(new TabListPlaceholder(
              "guild_" + type.name().substring("GUILD_".length()) + "_top_" + fixedPosition,
              top((ranking, user) -> GuildsPlugin.getInstance()
                  .getGuildsConfiguration().messagesWrapper.topGuildRankFormatMap.get(
                      normalPosition)
                  .replace("{GUILD_TAG}", ((GuildRanking) ranking).getOwner().getTag())
                  .replace("{POINTS}", String.valueOf(ranking.getPoints())), type, position)));
        }
      }
    }

    // Users
    register(new TabListPlaceholder("user_points",
        user(user -> String.valueOf(user.getUserRanking().getPoints()))));
    register(new TabListPlaceholder("user_kills",
        user(user -> String.valueOf(user.getUserRanking().getKills()))));
    register(new TabListPlaceholder("user_deaths",
        user(user -> String.valueOf(user.getUserRanking().getDeaths()))));
    register(new TabListPlaceholder("user_kill_streak",
        user(user -> String.valueOf(user.getUserRanking().getKillStreak()))));
    register(new TabListPlaceholder("user_kdr",
        user(user -> String.format("%.2f", user.getUserRanking().getKDR()))));
    register(new TabListPlaceholder("user_position",
        user(user -> String.valueOf(user.getUserRanking().getPosition()))));
    register(new TabListPlaceholder("user_proxy", player -> {
      final SectorUser sectorUser = SectorsPlugin.getInstance().getSectorUserFactory()
          .findUserByPlayer(player);
      return sectorUser != null ? String.format("proxy_%02d", sectorUser.getProxy().getIdentifier())
          : "";
    }));

    // Guilds
    register(new TabListPlaceholder("guild_tag", guild(Guild::getTag)));
    register(new TabListPlaceholder("guild_leader",
        guild(guild -> guild.getLeaderMember().getUser().getNickname())));
    register(new TabListPlaceholder("guild_points",
        guild(guild -> String.valueOf(guild.getGuildRanking().getPoints()))));
    register(new TabListPlaceholder("guild_kills",
        guild(guild -> String.valueOf(guild.getGuildRanking().getKills()))));
    register(new TabListPlaceholder("guild_deaths",
        guild(guild -> String.valueOf(guild.getGuildRanking().getDeaths()))));
    register(new TabListPlaceholder("guild_kdr",
        guild(guild -> String.format("%.2f", guild.getGuildRanking().getKDR()))));
    register(new TabListPlaceholder("guild_position",
        guild(guild -> String.valueOf(guild.getGuildRanking().getPosition()))));
    register(
        new TabListPlaceholder("guild_lives", guild(guild -> String.valueOf(guild.getLives()))));
    register(new TabListPlaceholder("guilds_size", ignored -> String.valueOf(
        GuildsPlugin.getInstance().getGuildFactory().getGuildMap().size())));

    // Sectors
    register(new TabListPlaceholder("sector_name",
        ignored -> SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getName()));
    register(new TabListPlaceholder("sector_tps", ignored -> formatTPS(
        SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getStatistics()
            .getTps())));
    register(new TabListPlaceholder("sector_load", ignored -> String.format("%.2f",
        SectorsPlugin.getInstance().getSectorFactory().getCurrentSector().getStatistics()
            .getLoad())));
    register(new TabListPlaceholder("sector_time", ignored -> FORMATTER.format(new Date())));
    register(new TabListPlaceholder("online", ignored -> String.valueOf(
        SectorsPlugin.getInstance().getSectorUserFactory().getUserMap().size())));

    // Players
    register(new TabListPlaceholder("player_name", Player::getName));
    register(new TabListPlaceholder("player_ping",
        player -> String.valueOf(((CraftPlayer) player).getHandle().ping)));
    register(new TabListPlaceholder("player_rank",
        player -> PlatformPlugin.getInstance().getPlatformUserFactory()
            .findUserByUniqueId(player.getUniqueId()).map(
                user -> user.getRank().getCurrentRank().getChatPrefix() + user.getRank()
                    .getCurrentRank().getName()).orElse("").toUpperCase()));
  }

  public static void register(final TabListPlaceholder placeholder) {
    if (!PLACEHOLDER_SET.add(placeholder)) {
      throw new IllegalStateException("This placeholder is already registered!");
    }
  }

  private static <T extends Ranking> Function<Player, String> top(
      final BiFunction<T, GuildUser, String> replacer, final RankingIdentifiableType type,
      final int position) {
    //noinspection unchecked
    return player -> GuildsPlugin.getInstance().getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).flatMap(
            guildUser -> GuildsPlugin.getInstance().getRankingFactory().findRanking(type, position)
                .map(ranking -> replacer.apply((T) ranking, guildUser))).orElse("");
  }

  private static Function<Player, String> guild(final Function<Guild, String> replacer) {
    return player -> GuildsPlugin.getInstance().getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).map(GuildUser::getGuild).map(replacer)
        .orElse("Brak");
  }

  private static Function<Player, String> user(final Function<GuildUser, String> replacer) {
    return player -> GuildsPlugin.getInstance().getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).map(replacer).orElse("");
  }

  private static String formatTPS(final double tps) {
    return (tps > 18D ? ChatColor.GREEN : (tps > 16D ? ChatColor.YELLOW : ChatColor.RED).toString())
        + (tps > 20D ? "*" : "") + Math.min(Math.round(tps * 100D) / 100D, 20D);
  }
}
