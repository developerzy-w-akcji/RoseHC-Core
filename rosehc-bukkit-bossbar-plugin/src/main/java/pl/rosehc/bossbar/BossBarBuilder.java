package pl.rosehc.bossbar;

import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public abstract class BossBarBuilder {

  final BossBarPacket packet = new BossBarPacket();

  BossBarBuilder(UUID uuid) {
    packet.setUuid(uuid);
  }

  public static Add add(UUID uuid) {
    return new Add(uuid);
  }

  public static Remove remove(UUID uuid) {
    return new Remove(uuid);
  }

  public static UpdateProgress updateProgress(UUID uuid) {
    return new UpdateProgress(uuid);
  }

  public static UpdateStyle updateStyle(UUID uuid) {
    return new UpdateStyle(uuid);
  }

  public static UpdateTitle updateTitle(UUID uuid) {
    return new UpdateTitle(uuid);
  }

  public UUID uuid() {
    return packet.getUuid();
  }

  protected void check() {
  }

  public BossBarPacket buildPacket() {
    check();
    return packet;
  }

  public static class Add extends BossBarBuilder {

    private Add(UUID uuid) {
      super(uuid);
      this.packet.setOperation(BarOperation.ADD);
    }

    public Add title(BaseComponent[] title) {
      this.packet.setTitle(ComponentSerializer.toString(title));
      return this;
    }

    public Add progress(float progress) {
      this.packet.setProgress(progress);
      return this;
    }

    public Add color(BarColor color) {
      this.packet.setColor(color);
      return this;
    }

    public Add style(BarStyle style) {
      this.packet.setStyle(style);
      return this;
    }

    @Override
    protected void check() {
      if (Objects.isNull(this.packet.getTitle())) {
        throw new IllegalArgumentException("missing title");
      }
      if (Objects.isNull(this.packet.getColor())) {
        throw new IllegalArgumentException("missing color");
      }
      if (Objects.isNull(this.packet.getStyle())) {
        throw new IllegalArgumentException("missing style");
      }
    }
  }

  public static class Remove extends BossBarBuilder {

    private Remove(UUID uuid) {
      super(uuid);
      this.packet.setOperation(BarOperation.REMOVE);
    }
  }

  public static class UpdateProgress extends BossBarBuilder {

    private UpdateProgress(UUID uuid) {
      super(uuid);
      this.packet.setOperation(BarOperation.UPDATE_PCT);
    }

    public UpdateProgress progress(float progress) {
      this.packet.setProgress(progress);
      return this;
    }
  }

  public static class UpdateStyle extends BossBarBuilder {

    private UpdateStyle(UUID uuid) {
      super(uuid);
      this.packet.setOperation(BarOperation.UPDATE_STYLE);
    }

    /**
     * Sets the color of this boss bar.
     *
     * @param color the color of the bar
     */
    public UpdateStyle color(BarColor color) {
      this.packet.setColor(color);
      return this;
    }

    /**
     * Sets the bar style of this boss bar
     *
     * @param style the style of the bar
     */
    public UpdateStyle style(BarStyle style) {
      this.packet.setStyle(style);
      return this;
    }

    @Override
    protected void check() {
      if (Objects.isNull(this.packet.getColor())) {
        throw new IllegalArgumentException("missing color");
      }
      if (Objects.isNull(this.packet.getStyle())) {
        throw new IllegalArgumentException("missing style");
      }
    }
  }

  public static class UpdateTitle extends BossBarBuilder {

    private UpdateTitle(UUID uuid) {
      super(uuid);
      this.packet.setOperation(BarOperation.UPDATE_NAME);
    }

    /**
     * Sets the title of this boss bar
     *
     * @param title the title of the bar
     */
    public UpdateTitle title(BaseComponent[] title) {
      this.packet.setTitle(ComponentSerializer.toString(title));
      return this;
    }

    @Override
    protected void check() {
      if (Objects.isNull(this.packet.getTitle())) {
        throw new IllegalArgumentException("missing title");
      }
    }
  }
}
