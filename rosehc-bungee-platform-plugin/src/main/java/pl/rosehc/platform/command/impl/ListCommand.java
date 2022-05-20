package pl.rosehc.platform.command.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.proxy.Proxy;
import pl.rosehc.sectors.sector.Sector;

/**
 * @author stevimeister on 30/01/2022
 **/
public final class ListCommand {

  private final PlatformPlugin plugin;

  public ListCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-list")
  @Command(value = "list", description = "Wyświetla liste proxy oraz sektorów")
  public void handleList(final @Sender CommandSender sender) {
    final StringBuilder builder = new StringBuilder();
    final List<Sector> sectors = new ArrayList<>(
        SectorsPlugin.getInstance().getSectorFactory().getSectorMap().values());
    final List<Proxy> proxies = new ArrayList<>(
        SectorsPlugin.getInstance().getProxyFactory().getProxyMap().values());
    sectors.sort(Comparator.comparing(Sector::getName));
    proxies.sort(Comparator.comparingInt(Proxy::getIdentifier));
    for (final Sector sector : sectors) {
      builder.append(this.plugin.getPlatformConfiguration().messagesWrapper.sectorListInfo.replace(
              "{SECTOR_NAME}", sector.getName())
          .replace("{SECTOR_ONLINE_PLAYERS}", String.valueOf(sector.getStatistics().getPlayers()))
          .replace("{FORMATTED_TPS}", this.format(sector.getStatistics().getTps()))
          .replace("{FORMATTED_LOAD}", String.format("%.2f", sector.getStatistics().getLoad())));
      builder.append('\n');
    }
    for (final Proxy proxy : proxies) {
      builder.append(this.plugin.getPlatformConfiguration().messagesWrapper.proxyListInfo.replace(
              "{PROXY_IDENTIFIER}", "proxy_" + String.format("%02d", proxy.getIdentifier()))
          .replace("{PROXY_ONLINE_PLAYERS}", String.valueOf(proxy.getPlayers()))
          .replace("{FORMATTED_LOAD}", String.format("%.2f", proxy.getLoad())));
      builder.append('\n');
    }

    builder.append(this.plugin.getPlatformConfiguration().messagesWrapper.globalListInfo.replace(
            "{GLOBAL_ONLINE_PLAYERS}",
            String.valueOf(proxies.stream().mapToInt(Proxy::getPlayers).sum()))
        .replace("{CURRENT_ONLINE_PLAYERS}",
            String.valueOf(this.plugin.getProxy().getOnlineCount())));
    ChatHelper.sendMessage(sender, builder.toString());
  }

  private String format(final double tps) {
    return (tps > 18D ? ChatColor.GREEN : (tps > 16D ? ChatColor.YELLOW : ChatColor.RED).toString())
        + (tps > 20D ? "*" : "") + Math.min(Math.round(tps * 100D) / 100D, 20D);
  }
}