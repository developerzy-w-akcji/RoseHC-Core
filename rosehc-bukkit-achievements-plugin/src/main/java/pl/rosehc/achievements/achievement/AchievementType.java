package pl.rosehc.achievements.achievement;

import java.util.function.Function;
import pl.rosehc.adapter.helper.TimeHelper;

@SuppressWarnings("SpellCheckingInspection")
public enum AchievementType {

  KILLS(value -> String.valueOf(value.intValue())), POINTS(
      value -> String.valueOf(value.intValue())), KILLSTREAK(
      value -> String.valueOf(value.intValue())),
  MINING_LEVEL(value -> String.valueOf(value.intValue())), SPEND_TIME(
      value -> TimeHelper.timeToString(value.longValue())), EATEN_GOLDEN_HEADS(
      value -> String.valueOf(value.intValue())),
  TRAVELED_KILOMETERS(
      value -> String.format("%.2f", value.floatValue() * 0.001F)), OPENED_MAGIC_CASES(
      value -> String.valueOf(value.intValue()));

  private final Function<Double, String> formatFunction;

  AchievementType(final Function<Double, String> formatFunction) {
    this.formatFunction = formatFunction;
  }

  public Function<Double, String> getFormatFunction() {
    return this.formatFunction;
  }
}
