package pl.rosehc.guilds.tablist;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.guilds.GuildsConfiguration;

public final class TabListFactory {

  private final Map<UUID, TabList> activeTabListMap = new ConcurrentHashMap<>();
  private volatile TabListElements[] tabListElements;

  public TabList findOrCreateTabList(final Player player) {
    return this.activeTabListMap.computeIfAbsent(player.getUniqueId(),
        ignored -> new TabList(player));
  }

  public synchronized TabListElements[] getTabListElements() {
    return this.tabListElements;
  }

  public void removeTabList(final Player player) {
    this.activeTabListMap.remove(player.getUniqueId());
  }

  public synchronized void updateTabListElements(final GuildsConfiguration configuration) {
    this.tabListElements = configuration.tabListElementsWrapperList.stream().map(
        wrapper -> new TabListElements(wrapper.elementsMap, wrapper.header, wrapper.footer,
            TimeHelper.timeFromString(wrapper.updateTime))).toArray(TabListElements[]::new);
  }
}
