package pl.rosehc.guilds.command.user.guild;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.guild.GuildRanking;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class GuildInfoCommand {

  private final GuildsPlugin plugin;

  public GuildInfoCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild info",
      "g info"}, async = true, description = "Wyświetla informacje o podanej gildii.")
  public void handleGuildInfo(final @Sender Player player, final @Name("tag") Guild guild) {
    final Map<UUID, SectorUser> sectorUserMap = SectorsPlugin.getInstance().getSectorUserFactory()
        .getUserMap();
    final GuildMember[] members = Arrays.copyOfRange(guild.getGuildMembers(), 1,
        guild.getGuildMembers().length);
    final StringBuilder builder = new StringBuilder();
    int onlineMembers = 0, offlineMembers = 0;
    Arrays.sort(members, Comparator.comparingInt(
        member -> member != null ? member.getGroup().isDeputy() ? 0 : 1 : -1));
    for (final GuildMember member : members) {
      if (member != null) {
        final boolean isOnline = sectorUserMap.containsKey(member.getUniqueId());
        builder.append(isOnline ? ChatColor.GREEN : ChatColor.RED)
            .append(member.getUser().getNickname());
        if (member.isDeputy()) {
          builder.append("*");
        }

        builder.append(", ");
        if (isOnline) {
          onlineMembers++;
        } else {
          offlineMembers++;
        }
      }
    }

    final String builtMembers = builder.toString();
    final String formattedMembers = builtMembers.endsWith(", ") ? builtMembers.substring(0,
        builtMembers.length() - ", ".length())
        : !builtMembers.isEmpty() ? builtMembers : ChatColor.RED + "Brak";
    final GuildMember leaderMember = guild.getLeaderMember();
    final GuildRanking guildRanking = guild.getGuildRanking();
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildInfo.replace("{CREATION_DATE}",
                TimeHelper.dateToString(guild.getCreationTime())).replace("{VALIDITY_TIME}",
                TimeHelper.timeToString(guild.getValidityTime() - System.currentTimeMillis()))
            .replace("{TNT_PROTECTION_TIME}",
                TimeHelper.timeToString(guild.getProtectionTime() - System.currentTimeMillis()))
            .replace("{LIVES}", String.valueOf(guild.getLives())).replace("{LEADER}",
                (sectorUserMap.containsKey(leaderMember.getUniqueId()) ? ChatColor.GREEN
                    : ChatColor.RED) + leaderMember.getUser().getNickname())
            .replace("{MEMBER_LIST}", formattedMembers)
            .replace("{ONLINE_MEMBERS}", String.valueOf(onlineMembers))
            .replace("{OFFLINE_MEMBERS}", String.valueOf(offlineMembers))
            .replace("{MAX_MEMBERS}", String.valueOf(members.length))
            .replace("{KILLS}", String.valueOf(guildRanking.getKills()))
            .replace("{DEATHS}", String.valueOf(guildRanking.getDeaths()))
            .replace("{POINTS}", String.valueOf(guildRanking.getPoints()))
            .replace("{KDR}", String.format("%.2f", guildRanking.getKDR()))
            .replace("{POSITION}", String.valueOf(guildRanking.getPosition())).replace("{ALLY_TAG}",
                guild.getAlliedGuild() != null ? guild.getAlliedGuild().getTag() : "Brak"));
  }
}
