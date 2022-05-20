package pl.rosehc.guilds.user;

import java.util.UUID;
import pl.rosehc.controller.wrapper.guild.GuildUserFightInfoSerializableWrapper;

public final class GuildUserFightInfo {

  private final UUID uniqueId;
  private long fightTime;

  public GuildUserFightInfo(final UUID uniqueId, final long fightTime) {
    this.uniqueId = uniqueId;
    this.fightTime = fightTime;
  }

  public static GuildUserFightInfo create(final GuildUserFightInfoSerializableWrapper wrapper) {
    return new GuildUserFightInfo(wrapper.getUniqueId(), wrapper.getFightTime());
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public long getFightTime() {
    return this.fightTime;
  }

  public void setFightTime(final long fightTime) {
    this.fightTime = fightTime;
  }
}
