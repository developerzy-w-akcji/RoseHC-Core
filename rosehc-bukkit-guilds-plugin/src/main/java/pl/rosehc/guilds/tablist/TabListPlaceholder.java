package pl.rosehc.guilds.tablist;

import java.util.function.Function;
import org.bukkit.entity.Player;

public final class TabListPlaceholder {

  private final String name;
  private final Function<Player, String> replacer;

  public TabListPlaceholder(final String name, final Function<Player, String> replacer) {
    this.name = name.toUpperCase();
    this.replacer = replacer;
  }

  public String getName() {
    return this.name;
  }

  public String replace(final Player player) {
    return this.replacer.apply(player);
  }
}
