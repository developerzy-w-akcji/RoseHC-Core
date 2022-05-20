package pl.rosehc.guilds.guild;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import pl.rosehc.guilds.ranking.Ranking;
import pl.rosehc.guilds.user.GuildUserRanking;

public final class GuildRanking extends Ranking implements Comparable<GuildRanking> {

  private final Guild owner;

  public GuildRanking(final Guild owner) {
    super(0, 0, 0);
    this.owner = owner;
  }

  @Override
  public int getPoints() {
    final Set<Integer> topPointsSet = this.collectTopRankings(Ranking::getPoints);
    int totalPoints = 0;
    for (final int points : topPointsSet) {
      totalPoints += points;
    }

    return totalPoints / topPointsSet.size();
  }

  @Override
  public int getKills() {
    final Set<Integer> topKillsSet = this.collectTopRankings(Ranking::getKills);
    int totalKills = 0;
    for (final int kills : topKillsSet) {
      totalKills += kills;
    }

    return totalKills / topKillsSet.size();
  }

  @Override
  public int getDeaths() {
    final Set<Integer> topDeathsSet = this.collectTopRankings(Ranking::getDeaths);
    int totalDeaths = 0;
    for (final int deaths : topDeathsSet) {
      totalDeaths += deaths;
    }

    return totalDeaths / topDeathsSet.size();
  }

  @Override
  public int compareTo(final GuildRanking ranking) {
    int result = Integer.compare(this.getPoints(), ranking.getPoints());
    if (result == 0) {
      result = this.owner.getName().compareTo(ranking.owner.getName());
    }

    return result;
  }

  public Guild getOwner() {
    return this.owner;
  }

  private Set<Integer> collectTopRankings(final Function<GuildUserRanking, Integer> collector) {
    final Set<Integer> memberRankingSet = new HashSet<>();
    final GuildMember[] guildMembers = this.owner.getGuildMembers().clone();
    for (final GuildMember guildMember : guildMembers) {
      if (guildMember == null) {
        continue;
      }

      if (guildMember.isLeader()) {
        memberRankingSet.add(collector.apply(guildMember.getUser().getUserRanking()));
        break;
      }
    }

    int iterations = 0;
    Arrays.sort(guildMembers, Comparator.comparingInt(
        member -> member != null ? -collector.apply(member.getUser().getUserRanking()) : -1));
    for (final GuildMember guildMember : guildMembers) {
      if (guildMember == null) {
        continue;
      }

      memberRankingSet.add(collector.apply(guildMember.getUser().getUserRanking()));
      if (iterations++ >= 5) {
        break;
      }
    }

    return memberRankingSet;
  }
}
