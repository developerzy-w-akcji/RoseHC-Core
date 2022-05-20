package pl.rosehc.guilds.ranking.recalculation;

import java.util.NavigableSet;
import pl.rosehc.guilds.ranking.Ranking;

@FunctionalInterface
public interface RankingRecalculationFunction<T extends Ranking> {

  NavigableSet<T> recalculate();
}
