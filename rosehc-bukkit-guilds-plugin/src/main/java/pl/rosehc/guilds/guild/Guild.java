package pl.rosehc.guilds.guild;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import pl.rosehc.adapter.helper.SerializeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildPistonsUpdatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserMessagePacket;
import pl.rosehc.controller.wrapper.guild.GuildMemberSerializableWrapper;
import pl.rosehc.controller.wrapper.guild.GuildPermissionTypeWrapper;
import pl.rosehc.controller.wrapper.guild.GuildSerializableWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.group.GuildGroup;
import pl.rosehc.guilds.guild.scanner.GuildBlockScanner;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;

public final class Guild {

  private final String name, tag;
  private final Map<UUID, GuildGroup> guildGroupMap;
  private final GuildMember[] guildMembers;
  private final GuildType guildType;
  private final GuildRegion guildRegion;
  private final GuildRanking guildRanking;
  private final Sector creationSector;
  private final long creationTime;

  private volatile LinkedList<GuildRegenerationBlockState> regenerationBlockStateList;
  private Map<UUID, Long> memberInviteMap;
  private Map<UUID, GuildPlayerHelpInfo> guildPlayerHelpInfoMap, allyPlayerHelpInfoMap;
  private Entry<String, Long> allyInviteEntry;
  private Guild alliedGuild;
  private GuildBlockScanner pistonBlockScanner;
  private Location homeLocation;
  private String joinAlertMessage;
  private long validityTime, protectionTime, tntExplosionTime, tntExplosionNotificationTime;
  private boolean pvpGuild, pvpAlly;
  private int lives, health, leaderMemberArrayPosition, pistonsOnGuild;

  private Guild(final GuildSerializableWrapper wrapper) {
    final GuildType guildType = wrapper.getGuildType().toOriginal();
    this.name = wrapper.getName();
    this.tag = wrapper.getTag();
    this.guildGroupMap = wrapper.getGuildGroupMap().values().stream().map(
            group -> new GuildGroup(group.getUniqueId(),
                group.getPermissions().stream().map(GuildPermissionTypeWrapper::toOriginal)
                    .collect(Collectors.toSet()), group.getColor().toOriginal(),
                GuildsPlugin.getInstance().getGuildGroupFactory().getLeaderGuildGroup().getUniqueId()
                    .equals(group.getUniqueId()),
                GuildsPlugin.getInstance().getGuildGroupFactory().getDeputyGuildGroup().getUniqueId()
                    .equals(group.getUniqueId()), group.getName()))
        .collect(Collectors.toConcurrentMap(GuildGroup::getUniqueId, group -> group));
    this.guildMembers = new GuildMember[guildType.getSize()];
    for (int slot = 0; slot < wrapper.getGuildMembers().length; slot++) {
      final GuildMemberSerializableWrapper member = wrapper.getGuildMembers()[slot];
      if (member != null) {
        this.guildMembers[slot] = new GuildMember(member.getUniqueId(),
            GuildsPlugin.getInstance().getGuildUserFactory()
                .findUserByUniqueId(member.getUniqueId())
                .orElseThrow(() -> new UnsupportedOperationException("Brak użytkownika!")),
            member.getPermissions().stream().map(GuildPermissionTypeWrapper::toOriginal)
                .collect(Collectors.toSet()),
            Optional.ofNullable(this.guildGroupMap.get(member.getGroupUniqueId())).orElseThrow(
                () -> new UnsupportedOperationException(
                    "Grupa o identyfikatorze " + member.getGroupUniqueId() + " nie istnieje!")));
        this.guildMembers[slot].getUser().setMemberArrayPosition(slot);
        this.guildMembers[slot].getUser().setGuild(this);
      }
    }

    this.guildType = guildType;
    this.guildRegion = new GuildRegion(Objects.requireNonNull(
        SerializeHelper.deserializeLocation(wrapper.getGuildRegion().getCenterLocation())),
        wrapper.getGuildRegion().getSize());
    this.guildRanking = new GuildRanking(this);
    this.creationSector = SectorsPlugin.getInstance().getSectorFactory()
        .findSector(wrapper.getCreationSectorName()).orElseThrow(
            () -> new UnsupportedOperationException(
                "Sektor o nazwie " + wrapper.getCreationSectorName() + " nie istnieje!"));
    this.memberInviteMap =
        wrapper.getMemberInviteMap() != null ? wrapper.getMemberInviteMap() : null;
    this.guildPlayerHelpInfoMap =
        wrapper.getGuildPlayerHelpInfoMap() != null ? wrapper.getGuildPlayerHelpInfoMap().entrySet()
            .stream().map(entry -> new SimpleEntry<>(entry.getKey(),
                new GuildPlayerHelpInfo(entry.getValue().getNickname(), entry.getValue().getTime(),
                    entry.getValue().getX(), entry.getValue().getY(), entry.getValue().getZ())))
            .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue)) : null;
    this.allyPlayerHelpInfoMap =
        wrapper.getAllyPlayerHelpInfoMap() != null ? wrapper.getAllyPlayerHelpInfoMap().entrySet()
            .stream().map(entry -> new SimpleEntry<>(entry.getKey(),
                new GuildPlayerHelpInfo(entry.getValue().getNickname(), entry.getValue().getTime(),
                    entry.getValue().getX(), entry.getValue().getY(), entry.getValue().getZ())))
            .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue)) : null;
    this.regenerationBlockStateList =
        wrapper.getRegenerationBlockStateList() != null ? wrapper.getRegenerationBlockStateList()
            .stream().map(GuildRegenerationBlockState::create)
            .collect(Collectors.toCollection(LinkedList::new)) : null;
    this.allyInviteEntry = wrapper.getAllyInviteEntry();
    this.homeLocation = SerializeHelper.deserializeLocation(wrapper.getHomeLocation());
    this.joinAlertMessage = wrapper.getJoinAlertMessage();
    this.creationTime = wrapper.getCreationTime();
    this.validityTime = wrapper.getValidityTime();
    this.protectionTime = wrapper.getProtectionTime();
    this.pvpGuild = wrapper.isPvpGuild();
    this.pvpAlly = wrapper.isPvpAlly();
    this.lives = wrapper.getLives();
    this.health = wrapper.getHealth();
    this.pistonsOnGuild = wrapper.getPistonsOnGuild();
    this.updateLeaderMemberPosition();
  }

  public Guild(final String name, final String tag, final Map<UUID, GuildGroup> guildGroupMap,
      final GuildMember leader, final GuildType guildType, final GuildRegion guildRegion,
      final Sector creationSector, final Location homeLocation, final long validityTime,
      final long protectionTime, final int lives) {
    this.name = name;
    this.tag = tag;
    this.guildGroupMap = guildGroupMap;
    this.guildMembers = new GuildMember[guildType.getSize()];
    this.guildMembers[0] = leader;
    this.guildMembers[0].getUser().setMemberArrayPosition(0);
    this.guildMembers[0].getUser().setGuild(this);
    this.guildType = guildType;
    this.guildRegion = guildRegion;
    this.homeLocation = homeLocation;
    this.guildRanking = new GuildRanking(this);
    this.creationSector = creationSector;
    this.creationTime = System.currentTimeMillis();
    this.validityTime = validityTime;
    this.protectionTime = protectionTime;
    this.pvpGuild = true;
    this.pvpAlly = true;
    this.lives = lives;
    this.health = GuildsPlugin.getInstance()
        .getGuildsConfiguration().pluginWrapper.guildStartHealth;
  }

  public static Guild create(final GuildSerializableWrapper wrapper) {
    return new Guild(wrapper);
  }

  public String getName() {
    return this.name;
  }

  public String getTag() {
    return this.tag;
  }

  public Map<UUID, GuildGroup> getGuildGroupMap() {
    return this.guildGroupMap;
  }

  public Optional<GuildGroup> findGuildGroup(final UUID uniqueId) {
    return Optional.ofNullable(this.guildGroupMap.get(uniqueId));
  }

  public GuildGroup getDefaultGroup() {
    final GuildGroup defaultGuildGroup = GuildsPlugin.getInstance().getGuildGroupFactory()
        .getDefaultGuildGroup();
    return Optional.ofNullable(this.guildGroupMap.get(defaultGuildGroup.getUniqueId())).orElseThrow(
        () -> new UnsupportedOperationException(
            "Domyślna grupa w gildii " + this.tag + " nie istnieje!"));
  }

  public GuildGroup getLeaderGroup() {
    final GuildGroup leaderGroup = GuildsPlugin.getInstance().getGuildGroupFactory()
        .getLeaderGuildGroup();
    return Optional.ofNullable(this.guildGroupMap.get(leaderGroup.getUniqueId())).orElseThrow(
        () -> new UnsupportedOperationException(
            "Domyślna grupa lidera w gildii " + this.tag + " nie istnieje!"));
  }

  public GuildGroup getDeputyGroup() {
    final GuildGroup deputyGroup = GuildsPlugin.getInstance().getGuildGroupFactory()
        .getDeputyGuildGroup();
    return Optional.ofNullable(this.guildGroupMap.get(deputyGroup.getUniqueId())).orElseThrow(
        () -> new UnsupportedOperationException(
            "Domyślna grupa zastępcy w gildii " + this.tag + " nie istnieje!"));
  }

  public GuildMember[] getGuildMembers() {
    return this.guildMembers;
  }

  public GuildMember getGuildMember(final GuildUser user) {
    return this.guildMembers[user.getMemberArrayPosition()];
  }

  public GuildMember getLeaderMember() {
    return this.guildMembers[this.leaderMemberArrayPosition];
  }

  public boolean addGuildMember(final GuildMember member) {
    int freeSlot = -1;
    for (final GuildMember targetMember : this.guildMembers) {
      if (targetMember != null && targetMember.getUniqueId().equals(member.getUniqueId())) {
        return false;
      }
    }

    for (int slot = 0; slot < this.guildMembers.length; slot++) {
      if (Objects.isNull(this.guildMembers[slot])) {
        freeSlot = slot;
        break;
      }
    }

    if (freeSlot != -1) {
      this.guildMembers[freeSlot] = member;
      this.guildMembers[freeSlot].getUser().setMemberArrayPosition(freeSlot);
      this.guildMembers[freeSlot].getUser().setGuild(this);
      return true;
    }

    return false;
  }

  public int getCurrentMembersSize() {
    int size = 0;
    for (final GuildMember member : this.guildMembers) {
      if (member != null) {
        size++;
      }
    }

    return size;
  }

  public boolean isFull() {
    return this.getCurrentMembersSize() >= this.guildMembers.length;
  }

  public void removeGuildMember(final GuildUser member) {
    final int memberArrayPosition = member.getMemberArrayPosition();
    if (memberArrayPosition != -1) {
      this.guildMembers[memberArrayPosition].getUser().setGuild(null);
      this.guildMembers[memberArrayPosition].getUser().setMemberArrayPosition(-1);
      this.guildMembers[memberArrayPosition] = null;
    }
  }

  public void updateLeaderMemberPosition() {
    for (int position = 0; position < this.guildMembers.length; position++) {
      final GuildMember member = this.guildMembers[position];
      if (member != null && member.isLeader()) {
        this.leaderMemberArrayPosition = position;
        break;
      }
    }
  }

  public GuildType getGuildType() {
    return this.guildType;
  }

  public GuildRegion getGuildRegion() {
    return this.guildRegion;
  }

  public Location getHomeLocation() {
    return this.homeLocation;
  }

  public void setHomeLocation(final Location homeLocation) {
    this.homeLocation = homeLocation;
  }

  public String getJoinAlertMessage() {
    return this.joinAlertMessage;
  }

  public void setJoinAlertMessage(final String joinAlertMessage) {
    this.joinAlertMessage = joinAlertMessage;
  }

  public GuildRanking getGuildRanking() {
    return this.guildRanking;
  }

  public void broadcastChatMessage(final String message) {
    final List<UUID> uniqueIdList = new ArrayList<>();
    for (final GuildMember member : this.guildMembers) {
      if (member != null && SectorsPlugin.getInstance().getSectorUserFactory()
          .findUserByUniqueId(member.getUniqueId()).isPresent()) {
        uniqueIdList.add(member.getUniqueId());
      }
    }

    if (!uniqueIdList.isEmpty()) {
      PlatformPlugin.getInstance().getRedisAdapter()
          .sendPacket(new PlatformUserMessagePacket(uniqueIdList, message), "rhc_platform");
    }
  }

  public boolean isMemberInvited(final GuildUser user) {
    if (this.memberInviteMap == null) {
      return false;
    }

    return this.memberInviteMap.containsKey(user.getUniqueId())
        && this.memberInviteMap.get(user.getUniqueId()) > System.currentTimeMillis();
  }

  public void addMemberInvite(final GuildUser user, final long time) {
    if (this.memberInviteMap == null) {
      this.memberInviteMap = new ConcurrentHashMap<>();
    }

    this.memberInviteMap.put(user.getUniqueId(), time);
  }

  public void removeMemberInvite(final GuildUser user) {
    if (this.memberInviteMap != null) {
      this.memberInviteMap.remove(user.getUniqueId());
    }
  }

  public Map<UUID, GuildPlayerHelpInfo> getGuildPlayerHelpInfoMap() {
    return this.guildPlayerHelpInfoMap;
  }

  public Map<UUID, GuildPlayerHelpInfo> getAllyPlayerHelpInfoMap() {
    return this.allyPlayerHelpInfoMap;
  }

  public Optional<GuildPlayerHelpInfo> findGuildPlayerHelpInfo(final UUID uniqueId) {
    return this.guildPlayerHelpInfoMap != null ? Optional.ofNullable(
        this.guildPlayerHelpInfoMap.get(uniqueId)) : Optional.empty();
  }

  public void addGuildPlayerHelpInfo(final UUID uniqueId, final GuildPlayerHelpInfo info) {
    if (this.guildPlayerHelpInfoMap == null) {
      this.guildPlayerHelpInfoMap = new ConcurrentHashMap<>();
    }

    this.guildPlayerHelpInfoMap.put(uniqueId, info);
  }

  public void removeGuildPlayerHelpInfo(final UUID uniqueId) {
    if (this.guildPlayerHelpInfoMap != null) {
      this.guildPlayerHelpInfoMap.remove(uniqueId);
    }
  }

  public Optional<GuildPlayerHelpInfo> findGuildAllyPlayerHelpInfo(final UUID uniqueId) {
    return this.allyPlayerHelpInfoMap != null ? Optional.ofNullable(
        this.allyPlayerHelpInfoMap.get(uniqueId)) : Optional.empty();
  }

  public void addGuildAllyPlayerHelpInfo(final UUID uniqueId, final GuildPlayerHelpInfo info) {
    if (this.allyPlayerHelpInfoMap == null) {
      this.allyPlayerHelpInfoMap = new ConcurrentHashMap<>();
    }

    this.allyPlayerHelpInfoMap.put(uniqueId, info);
  }

  public void removeGuildAllyPlayerHelpInfo(final UUID uniqueId) {
    if (this.allyPlayerHelpInfoMap != null) {
      this.allyPlayerHelpInfoMap.remove(uniqueId);
    }
  }

  public synchronized List<GuildRegenerationBlockState> getNewlyAddedRegenerationBlocks() {
    if (Objects.nonNull(this.regenerationBlockStateList)) {
      final List<GuildRegenerationBlockState> regenerationBlockStateList = new ArrayList<>(
          this.regenerationBlockStateList);
      regenerationBlockStateList.removeIf(state -> !state.wasAdded());
      return regenerationBlockStateList;
    }

    return new ArrayList<>();
  }

  public synchronized Queue<GuildRegenerationBlockState> takeRegenerationBlocks() {
    final Queue<GuildRegenerationBlockState> queue = new ArrayDeque<>(
        this.regenerationBlockStateList);
    this.regenerationBlockStateList.clear();
    return queue;
  }

  public synchronized void addRegenerationBlocks(
      final Collection<GuildRegenerationBlockState> blocks) {
    if (Objects.isNull(this.regenerationBlockStateList)) {
      this.regenerationBlockStateList = new LinkedList<>();
    }

    this.regenerationBlockStateList.addAll(blocks);
  }

  public synchronized void addRegenerationBlock(final GuildRegenerationBlockState block) {
    if (Objects.isNull(this.regenerationBlockStateList)) {
      this.regenerationBlockStateList = new LinkedList<>();
    }

    this.regenerationBlockStateList.add(block);
  }

  public Entry<String, Long> getAllyInviteEntry() {
    return this.allyInviteEntry;
  }

  public void setAllyInviteEntry(final Entry<String, Long> allyInviteEntry) {
    this.allyInviteEntry = allyInviteEntry;
  }

  public Guild getAlliedGuild() {
    return this.alliedGuild;
  }

  public void setAlliedGuild(final Guild alliedGuild) {
    this.alliedGuild = alliedGuild;
  }

  public Sector getCreationSector() {
    return this.creationSector;
  }

  public long getCreationTime() {
    return this.creationTime;
  }

  public long getValidityTime() {
    return this.validityTime;
  }

  public void setValidityTime(final long validityTime) {
    this.validityTime = validityTime;
  }

  public long getProtectionTime() {
    return this.protectionTime;
  }

  public void setProtectionTime(final long protectionTime) {
    this.protectionTime = protectionTime;
  }

  public boolean canNotBuild() {
    return this.tntExplosionTime >= System.currentTimeMillis();
  }

  public boolean canSendTntExplosionNotification() {
    return this.tntExplosionNotificationTime < System.currentTimeMillis();
  }

  public void updateTntExplosionTime() {
    this.tntExplosionTime = System.currentTimeMillis() + GuildsPlugin.getInstance()
        .getGuildsConfiguration().pluginWrapper.parsedTntExplosionTime;
  }

  public void updateTntExplosionNotificationTime() {
    this.tntExplosionNotificationTime = System.currentTimeMillis() + GuildsPlugin.getInstance()
        .getGuildsConfiguration().pluginWrapper.parsedTntExplosionNotificationTime;
  }

  public boolean isPvpGuild() {
    return this.pvpGuild;
  }

  public void setPvpGuild(final boolean pvpGuild) {
    this.pvpGuild = pvpGuild;
  }

  public boolean isPvpAlly() {
    return this.pvpAlly;
  }

  public void setPvpAlly(final boolean pvpAlly) {
    this.pvpAlly = pvpAlly;
  }

  public int getLives() {
    return this.lives;
  }

  public void setLives(final int lives) {
    this.lives = lives;
  }

  public int getHealth() {
    return this.health;
  }

  public void setHealth(final int health) {
    this.health = health;
  }

  public GuildBlockScanner getPistonBlockScanner() {
    return this.pistonBlockScanner;
  }

  public int getPistonsOnGuild() {
    return this.pistonsOnGuild;
  }

  public void setPistonsOnGuild(final int pistonsOnGuild) {
    this.pistonsOnGuild = pistonsOnGuild;
  }

  public void startScanningPistons() {
    if (this.pistonBlockScanner != null) {
      this.pistonBlockScanner.cancel();
    }

    this.pistonBlockScanner = new GuildBlockScanner(new HashSet<>(Arrays.asList(
        Material.PISTON_BASE, Material.PISTON_EXTENSION,
        Material.PISTON_MOVING_PIECE, Material.PISTON_STICKY_BASE
    )), scannedMaterialMap -> {
      final int pistonsOnGuild = scannedMaterialMap.values().stream().mapToInt(AtomicInteger::get)
          .sum();
      this.pistonsOnGuild = pistonsOnGuild;
      this.pistonBlockScanner = null;
      GuildsPlugin.getInstance().getRedisAdapter()
          .sendPacket(new GuildPistonsUpdatePacket(this.tag, pistonsOnGuild),
              "rhc_master_controller", "rhc_guilds");
    }, this);
  }
}
