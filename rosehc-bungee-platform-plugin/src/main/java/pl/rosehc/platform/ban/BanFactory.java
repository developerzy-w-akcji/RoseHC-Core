package pl.rosehc.platform.ban;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import pl.rosehc.controller.packet.platform.ban.PlatformBanComputerUidUpdatePacket;
import pl.rosehc.controller.packet.platform.ban.PlatformBanIpUpdatePacket;
import pl.rosehc.controller.wrapper.platform.PlatformBanSerializableWrapper;
import pl.rosehc.platform.PlatformPlugin;

public final class BanFactory {

  private final Map<String, Ban> banMap;

  public BanFactory(final List<PlatformBanSerializableWrapper> bans) {
    this.banMap = new ConcurrentHashMap<>();
    for (final PlatformBanSerializableWrapper ban : bans) {
      this.banMap.put(ban.getPlayerNickname().toLowerCase(), Ban.create(ban));
    }

    PlatformPlugin.getInstance().getLogger()
        .log(Level.INFO, "Załadowano " + this.banMap.size() + " banów.");
  }

  public void addBan(final Ban ban) {
    this.banMap.put(ban.getPlayerNickname().toLowerCase(), ban);
  }

  public void removeBan(final Ban ban) {
    this.banMap.remove(ban.getPlayerNickname().toLowerCase());
  }

  public Optional<Ban> findBan(final String playerNickname, final String ip,
      final byte[] computerUid) {
    final Ban ban = this.banMap.get(playerNickname.toLowerCase());
    if (Objects.nonNull(ban)) {
      this.updateOtherCredentials(ban, ip, computerUid, ban.getIp().equals(ip),
          Arrays.equals(ban.getComputerUid(), computerUid));
      return Optional.of(ban);
    }

    for (final Ban otherBan : this.banMap.values()) {
      final boolean isIpRight = otherBan.getIp().equals(ip), isComputerUidRight =
          Objects.nonNull(computerUid) && Arrays.equals(otherBan.getComputerUid(), computerUid);
      if (isIpRight || isComputerUidRight) {
        this.updateOtherCredentials(otherBan, otherBan.getIp(), otherBan.getComputerUid(),
            isIpRight, isComputerUidRight);
        return Optional.of(otherBan);
      }
    }

    return Optional.empty();
  }

  public Optional<Ban> findBan(final String playerNickname) {
    return Optional.ofNullable(this.banMap.get(playerNickname.toLowerCase()));
  }

  public Map<String, Ban> getBanMap() {
    return this.banMap;
  }

  private void updateOtherCredentials(final Ban ban, final String ip, final byte[] computerUid,
      final boolean isIpRight, final boolean isComputerUidRight) {
    if (!isIpRight) {
      ban.setIp(ip);
      PlatformPlugin.getInstance().getRedisAdapter()
          .sendPacket(new PlatformBanIpUpdatePacket(ban.getPlayerNickname(), ip),
              "rhc_master_controller", "rhc_platform");
    }

    if (!isComputerUidRight && Objects.nonNull(computerUid)) {
      ban.setComputerUid(computerUid);
      PlatformPlugin.getInstance().getRedisAdapter()
          .sendPacket(new PlatformBanComputerUidUpdatePacket(ban.getPlayerNickname(), computerUid),
              "rhc_master_controller", "rhc_platform");
    }
  }
}
