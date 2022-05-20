package pl.rosehc.platform.listener.server;

import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.StringUtils;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformConfiguration.ProxyMotdWrapper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.proxy.Proxy;

public final class ProxyPingListener implements Listener {

  private final PlatformPlugin plugin;

  public ProxyPingListener(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPing(final ProxyPingEvent event) {
    final ServerPing response = event.getResponse();
    final Players responsePlayers = response.getPlayers();
    if (!SectorsPlugin.getInstance().isLoaded()) {
      responsePlayers.setOnline(0);
      responsePlayers.setMax(0);
      response.setDescriptionComponent(
          new TextComponent(ChatColor.RED + "Sektory wciąż się ładują."));
      return;
    }

    final ProxyMotdWrapper proxyMotdWrapper = this.plugin.getPlatformConfiguration().proxyMotdWrapper;
    final PlayerInfo[] samples = new PlayerInfo[proxyMotdWrapper.hoverLines.size()];
    final Proxy currentProxy = SectorsPlugin.getInstance().getProxyFactory().getCurrentProxy();
    final int players = proxyMotdWrapper.counterPlayersLimit >= 1 ? Math.min(
        SectorsPlugin.getInstance().getSectorUserFactory().getUserMap().size(),
        proxyMotdWrapper.counterPlayersLimit)
        : SectorsPlugin.getInstance().getSectorUserFactory().getUserMap().size(), maxPlayers =
        proxyMotdWrapper.counterPlayersLimit < 1 ?
            players < this.plugin.getPlatformConfiguration().slotWrapper.proxySlots
                ? this.plugin.getPlatformConfiguration().slotWrapper.proxySlots : players + 1
            : proxyMotdWrapper.counterPlayersLimit;
    for (int index = 0; index < samples.length; index++) {
      String line = proxyMotdWrapper.hoverLines.get(index);
      line = line.replace("{PROXY_IDENTIFIER}",
          String.format("%02d", currentProxy.getIdentifier()));
      line = line.replace("{PROXY_CPU_USAGE}", String.format("%.2f", currentProxy.getLoad()));
      line = line.replace("{COUNTER_PLAYERS_LIMIT}", String.valueOf(maxPlayers));
      samples[index] = new PlayerInfo(ChatHelper.colored(line), UUID.randomUUID().toString());
    }

    responsePlayers.setOnline(players);
    responsePlayers.setMax(maxPlayers);
    responsePlayers.setSample(samples);
    response.setVersion(new Protocol(ChatHelper.colored(
        (proxyMotdWrapper.thirdLine + StringUtils.repeat(' ', proxyMotdWrapper.thirdLineSpacing))
            + "&r" + proxyMotdWrapper.playersInfo.replace("{ONLINE_PLAYERS}",
            String.valueOf(players)).replace("{MAX_PLAYERS}", String.valueOf(maxPlayers))), 1337));
    response.setDescriptionComponent(new TextComponent(
        ChatHelper.colored(proxyMotdWrapper.firstLine + '\n' + proxyMotdWrapper.secondLine)));
  }
}
