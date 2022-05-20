package pl.rosehc.guilds.tablist;

import java.util.Map;
import org.bukkit.entity.Player;

public final class TabListElements {

  private final Map<Integer, String> elementMap;
  private final String header, footer;
  private final long updateTime;

  public TabListElements(final Map<Integer, String> elementMap, final String header,
      final String footer, final long updateTime) {
    this.elementMap = elementMap;
    this.header = header;
    this.footer = footer;
    this.updateTime = updateTime;
  }

  public String getElement(final Player player, final int index) {
    return TabListPlaceholders.replace(player, this.elementMap.getOrDefault(index, ""));
  }

  public String getHeader(final Player player) {
    return this.header != null ? TabListPlaceholders.replace(player, this.header) : null;
  }

  public String getFooter(final Player player) {
    return this.footer != null ? TabListPlaceholders.replace(player, this.footer) : null;
  }

  public long getUpdateTime() {
    return this.updateTime;
  }
}
