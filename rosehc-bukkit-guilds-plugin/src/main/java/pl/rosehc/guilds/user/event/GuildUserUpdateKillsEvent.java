package pl.rosehc.guilds.user.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.rosehc.guilds.user.GuildUser;

public final class GuildUserUpdateKillsEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private final GuildUser user;
  private int kills;

  public GuildUserUpdateKillsEvent(final GuildUser user, final int kills) {
    this.user = user;
    this.kills = kills;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public GuildUser getUser() {
    return this.user;
  }

  public int getKills() {
    return this.kills;
  }

  public void setKills(final int kills) {
    this.kills = kills;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
