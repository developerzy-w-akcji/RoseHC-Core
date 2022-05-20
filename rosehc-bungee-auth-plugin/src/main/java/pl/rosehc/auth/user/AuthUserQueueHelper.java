package pl.rosehc.auth.user;

import java.util.Objects;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.AdapterPlugin;
import pl.rosehc.controller.packet.auth.AuthQueueAddPacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.SectorHelper;
import pl.rosehc.sectors.sector.SectorType;

public final class AuthUserQueueHelper {

  private AuthUserQueueHelper() {
  }

  public static void addToQueue(final ProxiedPlayer player) {
    PlatformPlugin.getInstance().getPlatformUserFactory().findUserByUniqueId(player.getUniqueId())
        .ifPresent(user -> {
          String sectorName = AdapterPlugin.getInstance().getRedisAdapter()
              .get("rhc_player_sectors", player.getUniqueId().toString());
          if (Objects.isNull(sectorName)) {
            sectorName = SectorHelper.getRandomSector(SectorType.GAME).orElse(
                SectorHelper.getRandomSector(SectorType.GAME, ignored -> true).orElseThrow(
                    () -> new IllegalStateException(
                        "Nie znaleziono sektora dla gracza " + player.getName() + "!"))).getName();
            AdapterPlugin.getInstance().getRedisAdapter()
                .set("rhc_player_sectors", player.getUniqueId().toString(), sectorName);
          }

          AdapterPlugin.getInstance().getRedisAdapter().sendPacket(
              new AuthQueueAddPacket(player.getUniqueId(), sectorName,
                  user.getRank().getCurrentRank().getPriority(),
                  SectorsPlugin.getInstance().getProxyFactory().getCurrentProxy().getIdentifier()),
              "rhc_queue");
        });
  }
}
