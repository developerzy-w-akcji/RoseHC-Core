package pl.rosehc.guilds.ranking;

import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.GuildType;
import pl.rosehc.guilds.ranking.recalculation.GuildRecalculationFunction;
import pl.rosehc.guilds.ranking.recalculation.RankingRecalculationFunction;
import pl.rosehc.guilds.ranking.recalculation.UserRecalculationFunction;

public enum RankingIdentifiableType {

  USER_GAME(new UserRecalculationFunction(GuildsPlugin.getInstance())), GUILD_SMALL(
      new GuildRecalculationFunction(GuildsPlugin.getInstance(), GuildType.SMALL)),
  GUILD_MEDIUM(
      new GuildRecalculationFunction(GuildsPlugin.getInstance(), GuildType.MEDIUM)), GUILD_LARGE(
      new GuildRecalculationFunction(GuildsPlugin.getInstance(), GuildType.LARGE));

  private final RankingRecalculationFunction<?> recalculationFunction;

  RankingIdentifiableType(final RankingRecalculationFunction<?> recalculationFunction) {
    this.recalculationFunction = recalculationFunction;
  }

  public RankingRecalculationFunction<?> getRecalculationFunction() {
    return this.recalculationFunction;
  }
}
