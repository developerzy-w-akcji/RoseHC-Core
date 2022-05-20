package pl.rosehc.guilds.user.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildUserUpdateKillStreakEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private final GuildUser user;
  private int killStreak;

  public GuildUserUpdateKillStreakEvent(final GuildUser user, final int killStreak) {
    this.user = user;
    this.killStreak = killStreak;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public GuildUser getUser() {
    return this.user;
  }

  public int getKillStreak() {
    return this.killStreak;
  }

  public void setKillStreak(final int killStreak) {
    this.killStreak = killStreak;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
