package pl.rosehc.guilds.ranking;

import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RankingFactory {

  private final Map<RankingIdentifiableType, NavigableSet<Ranking>> rankingSetByIdentifiableMap = new ConcurrentHashMap<>();

  public synchronized void updateAll() {
    for (final RankingIdentifiableType type : RankingIdentifiableType.values()) {
      if (Objects.nonNull(type.getRecalculationFunction())) {
        this.update(type);
      }
    }
  }

  public <T extends Ranking> Optional<T> findRanking(final RankingIdentifiableType type,
      final int position) {
    final NavigableSet<Ranking> rankingSet = this.rankingSetByIdentifiableMap.get(type);
    if (Objects.isNull(rankingSet)) {
      return Optional.empty();
    }

    //noinspection unchecked
    return (Optional<T>) Optional.ofNullable(Iterables.get(rankingSet, position, null));
  }

  private void update(final RankingIdentifiableType type) {
    //noinspection rawtypes
    final NavigableSet recalculated = type.getRecalculationFunction().recalculate();
    int position = 0;
    for (final Object object : recalculated) {
      ((Ranking) object).setPosition(++position);
    }

    //noinspection unchecked
    this.rankingSetByIdentifiableMap.put(type, recalculated);
  }
}
