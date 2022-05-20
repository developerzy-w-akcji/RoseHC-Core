package pl.rosehc.platform.ban;

import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public final class BanDeleteEvent extends Event implements Cancellable {

  private final Ban ban;
  private boolean cancelled;

  public BanDeleteEvent(final Ban ban) {
    this.ban = ban;
  }

  public Ban getBan() {
    return this.ban;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }
}
