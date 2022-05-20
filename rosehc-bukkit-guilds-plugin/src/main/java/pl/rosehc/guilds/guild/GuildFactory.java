package pl.rosehc.guilds.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import pl.rosehc.controller.wrapper.guild.GuildSerializableWrapper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.sectors.sector.Sector;

public final class GuildFactory {

  private final Map<String, Guild> guildMap;

  public GuildFactory(final List<GuildSerializableWrapper> guilds) {
    this.guildMap = new ConcurrentHashMap<>();
    for (final GuildSerializableWrapper guild : guilds) {
      final Guild deserializedGuild = Guild.create(guild);
      this.guildMap.put(guild.getTag().toLowerCase(), deserializedGuild);
      this.guildMap.put(guild.getName().toLowerCase(), deserializedGuild);
    }

    for (final GuildSerializableWrapper guild : guilds) {
      if (guild.getAlliedGuild() == null) {
        continue;
      }

      final Guild firstGuild = this.guildMap.get(guild.getTag().toLowerCase());
      final Guild secondGuild = this.guildMap.get(guild.getAlliedGuild().toLowerCase());
      if (firstGuild != null && secondGuild != null) {
        firstGuild.setAlliedGuild(secondGuild);
        secondGuild.setAlliedGuild(firstGuild);
      }
    }

    GuildsPlugin.getInstance().getLogger()
        .log(Level.INFO, "Załadowano " + this.getGuildMap().size() + " gildii.");
  }

  public void registerGuild(final Guild guild) {
    this.guildMap.put(guild.getTag().toLowerCase(), guild);
    this.guildMap.put(guild.getName().toLowerCase(), guild);
  }

  public void unregisterGuild(final Guild guild) {
    this.guildMap.remove(guild.getTag().toLowerCase());
    this.guildMap.remove(guild.getName().toLowerCase());
  }

  public Optional<Guild> findGuildNear(final Location targetLocation) {
    final World world = targetLocation.getWorld();
    final int distance =
        (GuildsPlugin.getInstance().getGuildsConfiguration().pluginWrapper.maxGuildSize * 2)
            + GuildsPlugin.getInstance().getGuildsConfiguration().pluginWrapper.minGuildDistance;
    for (final Guild guild : this.guildMap.values()) {
      final Location centerLocation = guild.getGuildRegion().getCenterLocation();
      if (centerLocation.equals(targetLocation) || !centerLocation.getWorld().equals(world)) {
        continue;
      }

      targetLocation.setY(centerLocation.getY());
      if (centerLocation.distanceSquared(targetLocation) <= distance) {
        return Optional.of(guild);
      }
    }

    return Optional.empty();
  }

  public Optional<Guild> findGuildByCredential(final String credential, final boolean tag) {
    Guild guild = this.guildMap.get(credential.toLowerCase());
    if (Objects.nonNull(guild)) {
      if (guild.getTag().equalsIgnoreCase(credential) && !tag) {
        return Optional.empty();
      } else if (guild.getName().equalsIgnoreCase(credential) && tag) {
        return Optional.empty();
      }
    }

    return Optional.ofNullable(guild);
  }

  public List<Guild> getGuildsBySector(final Sector sector) {
    final List<Guild> guildList = new ArrayList<>(this.guildMap.values());
    guildList.removeIf(guild -> !guild.getCreationSector().equals(sector));
    return guildList;
  }

  public Optional<Guild> findGuildInside(final Location targetLocation) {
    for (final Guild guild : this.guildMap.values()) {
      if (guild.getGuildRegion().isInside(targetLocation)) {
        return Optional.of(guild);
      }
    }

    return Optional.empty();
  }

  public Optional<Guild> findGuildByCredential(final String credential) {
    return this.findGuildByCredential(credential, true);
  }

  public Map<String, Guild> getGuildMap() {
    final Map<String, Guild> guildMap = new ConcurrentHashMap<>();
    for (final Entry<String, Guild> entry : this.guildMap.entrySet()) {
      if (entry.getValue().getTag().equals(entry.getKey())) {
        guildMap.put(entry.getKey(), entry.getValue());
      }
    }

    return guildMap;
  }
}
