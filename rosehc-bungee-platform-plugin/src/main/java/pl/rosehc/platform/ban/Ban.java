package pl.rosehc.platform.ban;

import pl.rosehc.controller.wrapper.platform.PlatformBanSerializableWrapper;

public final class Ban {

  private final String playerNickname, staffNickname, reason;
  private final long creationTime, leftTime;

  private String ip;
  private byte[] computerUid;

  public Ban(final String playerNickname, final String staffNickname, final String ip,
      final String reason, final byte[] computerUid, final long creationTime, final long leftTime) {
    this.playerNickname = playerNickname;
    this.staffNickname = staffNickname;
    this.ip = ip;
    this.reason = reason;
    this.computerUid = computerUid;
    this.creationTime = creationTime;
    this.leftTime = leftTime;
  }

  private Ban(final PlatformBanSerializableWrapper wrapper) {
    this(wrapper.getPlayerNickname(), wrapper.getStaffNickname(), wrapper.getIp(),
        wrapper.getReason(), wrapper.getComputerUid(), wrapper.getCreationTime(),
        wrapper.getLeftTime());
  }

  public static Ban create(final PlatformBanSerializableWrapper wrapper) {
    return new Ban(wrapper);
  }

  public String getPlayerNickname() {
    return this.playerNickname;
  }

  public String getStaffNickname() {
    return this.staffNickname;
  }

  public String getIp() {
    return this.ip;
  }

  public void setIp(final String ip) {
    this.ip = ip;
  }

  public String getReason() {
    return this.reason;
  }

  public byte[] getComputerUid() {
    return this.computerUid;
  }

  public void setComputerUid(final byte[] computerUid) {
    this.computerUid = computerUid;
  }

  public long getCreationTime() {
    return this.creationTime;
  }

  public long getLeftTime() {
    return this.leftTime;
  }

  public boolean isPerm() {
    return this.leftTime == 0L;
  }
}
