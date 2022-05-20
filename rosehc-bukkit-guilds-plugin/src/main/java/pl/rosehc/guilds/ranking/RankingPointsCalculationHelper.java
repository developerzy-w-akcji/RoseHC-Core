package pl.rosehc.guilds.ranking;

import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import pl.rosehc.guilds.GuildsConfiguration.PluginWrapper;
import tk.pratanumandal.expr4j.ExpressionEvaluator;

public final class RankingPointsCalculationHelper {

  private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();

  private RankingPointsCalculationHelper() {
  }

  public static Pair<Integer, Integer> calculatePoints(final PluginWrapper wrapper,
      final int victimPoints, final int killerPoints) {
    final String victimPointsCalculation = wrapper.victimPointsCalculation;
    final String killerPointsCalculation = wrapper.killerPointsCalculation;
    if (Objects.nonNull(victimPointsCalculation) && !victimPointsCalculation.trim().isEmpty()
        && !victimPointsCalculation.equalsIgnoreCase("NO_CALCULATION") && Objects.nonNull(
        killerPointsCalculation) && !killerPointsCalculation.trim().isEmpty()
        && !killerPointsCalculation.equalsIgnoreCase("NO_CALCULATION")) {
      final double evaluatedKillerPointsCalculation = EVALUATOR.evaluate(
          killerPointsCalculation.replace("{VICTIM_POINTS}", String.valueOf(victimPoints))
              .replace("{KILLER_POINTS}", String.valueOf(killerPoints)));
      final double evaluatedVictimPointsCalculation = EVALUATOR.evaluate(
          victimPointsCalculation.replace("{VICTIM_POINTS}", String.valueOf(victimPoints))
              .replace("{KILLER_POINTS}", String.valueOf(killerPoints))
              .replace("{CHANGED_KILLER_POINTS}",
                  String.valueOf((int) evaluatedKillerPointsCalculation)));
      return Pair.of((int) evaluatedKillerPointsCalculation,
          (int) evaluatedVictimPointsCalculation);
    }

    final String killerAndVictimPointsCalculation = wrapper.killerAndVictimPointsCalculation;
    final double evaluatedKillerAndVictimPointsCalculation = EVALUATOR.evaluate(
        killerAndVictimPointsCalculation.replace("{VICTIM_POINTS}", String.valueOf(victimPoints))
            .replace("{KILLER_POINTS}", String.valueOf(killerPoints)));
    return Pair.of((int) evaluatedKillerAndVictimPointsCalculation,
        (int) evaluatedKillerAndVictimPointsCalculation);
  }
}
