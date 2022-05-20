package pl.rosehc.controller.wrapper.platform;

import java.util.Map;
import java.util.UUID;

public final class PlatformUserSerializableWrapper {

  private UUID uniqueId;
  private Map<PlatformUserCooldownType, Long> cooldownMap;
  private String nickname;
  private String previousRankName, currentRankName;
  private byte[] computerUid;
  private long rankExpirationTime;

  private PlatformUserSerializableWrapper() {
  }

  public PlatformUserSerializableWrapper(final UUID uniqueId,
      final Map<PlatformUserCooldownType, Long> cooldownMap, final String nickname,
      final String previousRankName, final String currentRankName, final byte[] computerUid,
      final long rankExpirationTime) {
    this.uniqueId = uniqueId;
    this.cooldownMap = cooldownMap;
    this.nickname = nickname;
    this.previousRankName = previousRankName;
    this.currentRankName = currentRankName;
    this.computerUid = computerUid;
    this.rankExpirationTime = rankExpirationTime;
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public Map<PlatformUserCooldownType, Long> getCooldownMap() {
    return this.cooldownMap;
  }

  public String getNickname() {
    return this.nickname;
  }

  public String getPreviousRankName() {
    return this.previousRankName;
  }

  public String getCurrentRankName() {
    return this.currentRankName;
  }

  public byte[] getComputerUid() {
    return this.computerUid;
  }

  public long getRankExpirationTime() {
    return this.rankExpirationTime;
  }
}
