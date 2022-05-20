package pl.rosehc.guilds.user;

import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.ranking.Ranking;

public final class GuildUserRanking extends Ranking implements Comparable<GuildUserRanking> {

  private final GuildUser owner;
  private int killStreak;

  public GuildUserRanking(final GuildUser owner, final int points, final int kills,
      final int deaths, final int killStreak) {
    super(points, kills, deaths);
    this.owner = owner;
    this.killStreak = killStreak;
  }

  public GuildUser getOwner() {
    return this.owner;
  }

  public int getKillStreak() {
    return this.killStreak;
  }

  public void setKillStreak(final int killStreak) {
    this.killStreak = killStreak;
  }

  @Override
  public int compareTo(final GuildUserRanking ranking) {
    int result = Integer.compare(this.getPoints(), ranking.getPoints());
    if (result == 0) {
      result = this.owner.getNickname().compareTo(ranking.owner.getNickname());
    }

    return result;
  }

  public void reset() {
    this.setPoints(
        GuildsPlugin.getInstance().getGuildsConfiguration().pluginWrapper.startUserPoints);
    this.setKills(0);
    this.setDeaths(0);
    this.setKillStreak(0);
  }
}
