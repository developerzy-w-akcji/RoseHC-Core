package pl.rosehc.actionbar;

public final class PrioritizedActionBar implements Comparable<PrioritizedActionBar> {

  private final int priority;
  private String text;
  private long timeoutTime;

  public PrioritizedActionBar(final String text, final int priority) {
    this.text = text;
    this.timeoutTime = System.currentTimeMillis() + 2000L;
    this.priority = priority;
  }

  public String getText() {
    return this.text;
  }

  public void setText(final String text) {
    this.text = text;
    this.timeoutTime = System.currentTimeMillis() + 2000L;
  }

  public int getPriority() {
    return this.priority;
  }

  public boolean hasTimedOut() {
    return this.timeoutTime <= System.currentTimeMillis();
  }

  @Override
  public int compareTo(final PrioritizedActionBar other) {
    return Integer.compare(this.priority, other.priority);
  }
}
