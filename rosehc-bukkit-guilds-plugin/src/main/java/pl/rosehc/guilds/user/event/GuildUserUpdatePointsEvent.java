package pl.rosehc.guilds.user.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildUserUpdatePointsEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private final GuildUser user;
  private final boolean death;
  private int points;

  public GuildUserUpdatePointsEvent(final GuildUser user, final boolean death, final int points) {
    this.user = user;
    this.death = death;
    this.points = points;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public GuildUser getUser() {
    return this.user;
  }

  public boolean isDeath() {
    return this.death;
  }

  public int getPoints() {
    return this.points;
  }

  public void setPoints(final int points) {
    this.points = points;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
