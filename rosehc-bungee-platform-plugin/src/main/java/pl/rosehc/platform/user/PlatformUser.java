package pl.rosehc.platform.user;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import pl.rosehc.controller.wrapper.platform.PlatformUserSerializableWrapper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.rank.Rank;
import pl.rosehc.platform.rank.RankEntry;
import pl.rosehc.platform.rank.RankFactory;

public final class PlatformUser {

  private final UUID uniqueId;
  private final PlatformUserCooldownCache cooldownCache;

  private String nickname;
  private RankEntry rank;
  private byte[] computerUid;
  private boolean newlyCreated;

  private PlatformUser(final PlatformUserSerializableWrapper wrapper) {
    final String previousRankName = wrapper.getPreviousRankName();
    final String currentRankName = wrapper.getCurrentRankName();
    final RankFactory rankFactory = PlatformPlugin.getInstance().getRankFactory();
    final Optional<Rank> previousRank =
        previousRankName != null ? rankFactory.findRank(previousRankName) : Optional.empty();
    final Optional<Rank> currentRank =
        currentRankName != null ? rankFactory.findRank(currentRankName) : Optional.empty();
    final long rankExpirationTime = wrapper.getRankExpirationTime();
    this.uniqueId = wrapper.getUniqueId();
    this.cooldownCache = new PlatformUserCooldownCache(this, wrapper.getCooldownMap());
    this.nickname = wrapper.getNickname();
    this.rank = RankEntry.create(previousRank.orElse(null),
        currentRank.orElse(rankFactory.getDefaultRank()),
        previousRank.map(ignored -> rankExpirationTime).orElse(0L));
    this.computerUid = wrapper.getComputerUid();
  }

  public PlatformUser(final UUID uniqueId, final String nickname) {
    this.uniqueId = uniqueId;
    this.cooldownCache = new PlatformUserCooldownCache(this, new ConcurrentHashMap<>());
    this.nickname = nickname;
    this.rank = RankEntry.create(null,
        PlatformPlugin.getInstance().getRankFactory().getDefaultRank(), 0L);
  }

  public static PlatformUser create(final PlatformUserSerializableWrapper wrapper) {
    return new PlatformUser(wrapper);
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public PlatformUserCooldownCache getCooldownCache() {
    return this.cooldownCache;
  }

  public String getNickname() {
    return this.nickname;
  }

  public void setNickname(final String nickname) {
    this.nickname = nickname;
  }

  public RankEntry getRank() {
    return this.rank;
  }

  public void setRank(final RankEntry rank) {
    this.rank = rank;
  }

  public byte[] getComputerUid() {
    return this.computerUid;
  }

  public void setComputerUid(final byte[] computerUid) {
    this.computerUid = computerUid;
  }

  public boolean isNewlyCreated() {
    return this.newlyCreated;
  }

  public void setNewlyCreated(final boolean newlyCreated) {
    this.newlyCreated = newlyCreated;
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || this.getClass() != object.getClass()) {
      return false;
    }

    final PlatformUser user = (PlatformUser) object;
    return this.uniqueId.equals(user.uniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId);
  }
}
