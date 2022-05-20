package pl.rosehc.guilds.ranking.recalculation;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildRanking;
import pl.rosehc.guilds.guild.GuildType;

public final class GuildRecalculationFunction implements
    RankingRecalculationFunction<GuildRanking> {

  private final GuildsPlugin plugin;
  private final GuildType guildType;

  public GuildRecalculationFunction(final GuildsPlugin plugin, final GuildType guildType) {
    this.plugin = plugin;
    this.guildType = guildType;
  }

  @Override
  public NavigableSet<GuildRanking> recalculate() {
    final NavigableSet<GuildRanking> guildRankingSet = new TreeSet<>(Collections.reverseOrder());
    for (final Guild guild : this.plugin.getGuildFactory().getGuildMap().values()) {
      if (guild.getGuildType().equals(this.guildType)) {
        guildRankingSet.add(guild.getGuildRanking());
      }
    }

    return guildRankingSet;
  }
}
