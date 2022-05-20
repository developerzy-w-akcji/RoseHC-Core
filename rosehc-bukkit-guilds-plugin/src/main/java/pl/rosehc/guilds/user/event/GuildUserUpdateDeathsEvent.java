package pl.rosehc.guilds.user.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildUserUpdateDeathsEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private final GuildUser user;
  private int deaths;

  public GuildUserUpdateDeathsEvent(final GuildUser user, final int deaths) {
    this.user = user;
    this.deaths = deaths;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public GuildUser getUser() {
    return this.user;
  }

  public int getDeaths() {
    return this.deaths;
  }

  public void setDeaths(final int deaths) {
    this.deaths = deaths;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
