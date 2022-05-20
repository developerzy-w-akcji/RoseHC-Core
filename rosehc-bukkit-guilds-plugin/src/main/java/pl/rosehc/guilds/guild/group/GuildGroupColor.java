package pl.rosehc.guilds.guild.group;

import org.bukkit.ChatColor;

public enum GuildGroupColor {

  WHITE(ChatColor.WHITE.toString(), (byte) 0), ORANGE(ChatColor.GOLD.toString(), (byte) 1),
  MAGENTA(ChatColor.DARK_PURPLE.toString(), (byte) 2), LIGHT_BLUE(ChatColor.AQUA.toString(),
      (byte) 3),
  YELLOW(ChatColor.YELLOW.toString(), (byte) 4), LIME(ChatColor.GREEN.toString(), (byte) 5),
  PINK(ChatColor.LIGHT_PURPLE.toString(), (byte) 6), GRAY(ChatColor.DARK_GRAY.toString(), (byte) 7),
  LIGHT_GRAY(ChatColor.GRAY.toString(), (byte) 8), CYAN(ChatColor.BLUE.toString(), (byte) 9),
  PURPLE(ChatColor.DARK_PURPLE.toString(), (byte) 10), BLUE(ChatColor.DARK_BLUE.toString(),
      (byte) 11),
  BROWN(ChatColor.DARK_GRAY.toString(), (byte) 12), GREEN(ChatColor.DARK_GREEN.toString(),
      (byte) 13),
  RED(ChatColor.RED.toString(), (byte) 14), BLACK(ChatColor.BLACK.toString(), (byte) 15);

  private final String chatColor;
  private final int stainedClayData;

  GuildGroupColor(final String chatColor, final byte stainedClayData) {
    this.chatColor = chatColor;
    this.stainedClayData = stainedClayData;
  }

  public String getChatColor() {
    return this.chatColor;
  }

  public int getStainedClayData() {
    return this.stainedClayData;
  }
}
