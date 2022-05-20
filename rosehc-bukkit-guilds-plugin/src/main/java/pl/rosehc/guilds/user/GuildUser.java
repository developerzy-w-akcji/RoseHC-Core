package pl.rosehc.guilds.user;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.minecraft.server.v1_8_R3.EntityGolem;
import pl.rosehc.controller.packet.platform.user.PlatformUserMessagePacket;
import pl.rosehc.controller.wrapper.guild.GuildUserSerializableWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;

public final class GuildUser {

  private final UUID uniqueId;
  private final GuildUserRanking userRanking;
  private final Map<UUID, GuildUserFightInfo> fightInfoMap;
  private final Map<UUID, Long> victimsMap;

  private volatile EntityGolem guildGolem;
  private Guild guild, enteredGuild;
  private String nickname;
  private long guildDeletionPreparationTime;
  private int memberArrayPosition = -1;

  private GuildUser(final GuildUserSerializableWrapper wrapper) {
    this.uniqueId = wrapper.getUniqueId();
    this.userRanking = new GuildUserRanking(this, wrapper.getPoints(),
        wrapper.getKills(), wrapper.getDeaths(), wrapper.getKillStreak());
    this.nickname = wrapper.getNickname();
    this.fightInfoMap = wrapper.getFightInfoMap().entrySet().stream().map(
            entry -> new SimpleEntry<>(entry.getKey(), GuildUserFightInfo.create(entry.getValue())))
        .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));
    this.victimsMap = wrapper.getVictimMap();
  }

  public GuildUser(final UUID uniqueId, final String nickname) {
    this.uniqueId = uniqueId;
    this.nickname = nickname;
    this.userRanking = new GuildUserRanking(this,
        GuildsPlugin.getInstance().getGuildsConfiguration().pluginWrapper.startUserPoints, 0, 0, 0);
    this.fightInfoMap = new ConcurrentHashMap<>();
    this.victimsMap = new ConcurrentHashMap<>();
  }

  public static GuildUser create(final GuildUserSerializableWrapper wrapper) {
    return new GuildUser(wrapper);
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public GuildUserRanking getUserRanking() {
    return this.userRanking;
  }

  public Optional<GuildUserFightInfo> findLastOptionalFighter() {
    GuildUserFightInfo lastOptionalFightInfo = null;
    for (final GuildUserFightInfo fightInfo : this.fightInfoMap.values()) {
      if (lastOptionalFightInfo == null
          || lastOptionalFightInfo.getFightTime() <= fightInfo.getFightTime()) {
        lastOptionalFightInfo = fightInfo;
      }
    }

    return Optional.ofNullable(lastOptionalFightInfo);
  }

  public void cacheFighter(final UUID uniqueId, final long fightTime) {
    final GuildUserFightInfo fightInfo = this.fightInfoMap.get(uniqueId);
    if (Objects.nonNull(fightInfo)) {
      fightInfo.setFightTime(fightTime);
      this.fightInfoMap.replace(uniqueId, fightInfo);
    } else {
      this.fightInfoMap.put(uniqueId, new GuildUserFightInfo(uniqueId, fightTime));
    }
  }

  public void sendMessage(final String message) {
    SectorsPlugin.getInstance().getSectorUserFactory().findUserByUniqueId(this.uniqueId).ifPresent(
        user -> PlatformPlugin.getInstance().getRedisAdapter().sendPacket(
            new PlatformUserMessagePacket(new ArrayList<>(Collections.singletonList(this.uniqueId)),
                message), "rhc_platform_" + user.getSector().getName()));
  }

  public void clearFighters() {
    this.fightInfoMap.clear();
  }

  public long getAttackerTime(final GuildUser user) {
    return this.victimsMap.containsKey(user.getUniqueId())
        && this.victimsMap.get(user.getUniqueId()) > System.currentTimeMillis()
        ? this.victimsMap.get(user.getUniqueId()) : 0L;
  }

  public long getVictimTime(final GuildUser user) {
    return user.victimsMap.containsKey(this.uniqueId)
        && user.victimsMap.get(this.uniqueId) > System.currentTimeMillis() ? user.victimsMap.get(
        this.uniqueId) : 0L;
  }

  public void cacheVictim(final UUID uniqueId, final long time) {
    this.victimsMap.put(uniqueId, time);
  }

  public String getNickname() {
    return this.nickname;
  }

  public void setNickname(final String nickname) {
    this.nickname = nickname;
  }

  public Guild getGuild() {
    return this.guild;
  }

  public void setGuild(final Guild guild) {
    this.guild = guild;
  }

  public Guild getEnteredGuild() {
    return this.enteredGuild;
  }

  public void setEnteredGuild(final Guild enteredGuild) {
    this.enteredGuild = enteredGuild;
  }

  public synchronized EntityGolem getGuildGolem() {
    return this.guildGolem;
  }

  public synchronized void setGuildGolem(final EntityGolem guildGolem) {
    this.guildGolem = guildGolem;
  }

  public boolean isPreparedForGuildDeletion() {
    return this.guildDeletionPreparationTime + TimeUnit.MINUTES.toMillis(1L)
        > System.currentTimeMillis();
  }

  public void setPreparedForGuildDeletion(final boolean preparedForGuildDeletion) {
    this.guildDeletionPreparationTime = preparedForGuildDeletion ? System.currentTimeMillis() : 0L;
  }

  public int getMemberArrayPosition() {
    return this.memberArrayPosition;
  }

  public void setMemberArrayPosition(final int memberArrayPosition) {
    this.memberArrayPosition = memberArrayPosition;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }

    final GuildUser user = (GuildUser) other;
    return this.uniqueId.equals(user.uniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId);
  }
}
