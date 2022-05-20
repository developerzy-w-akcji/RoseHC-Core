package pl.rosehc.guilds.ranking.recalculation;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.guilds.user.GuildUserRanking;

public final class UserRecalculationFunction implements
    RankingRecalculationFunction<GuildUserRanking> {

  private final GuildsPlugin plugin;

  public UserRecalculationFunction(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public NavigableSet<GuildUserRanking> recalculate() {
    final NavigableSet<GuildUserRanking> userRankingSet = new TreeSet<>(Collections.reverseOrder());
    for (final GuildUser user : this.plugin.getGuildUserFactory().getGuildUserMap().values()) {
      userRankingSet.add(user.getUserRanking());
    }

    return userRankingSet;
  }
}
