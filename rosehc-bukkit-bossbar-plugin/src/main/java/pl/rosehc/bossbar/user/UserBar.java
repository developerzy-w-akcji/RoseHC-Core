package pl.rosehc.bossbar.user;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pl.rosehc.bossbar.BarColor;
import pl.rosehc.bossbar.BarStyle;
import pl.rosehc.bossbar.BossBarBuilder;

/**
 * @author stevimeister on 05.02.2021
 **/
public final class UserBar {

  private final UUID uniqueId;
  private final Set<UserBarType> userBarSet = ConcurrentHashMap.newKeySet();

  public UserBar(final Player player) {
    this.uniqueId = player.getUniqueId();
  }

  public void addBossBar(final UserBarType barType, final BossBarBuilder bossBarBuilder) {
    this.sendPacket(new PacketPlayOutCustomPayload("BP|UpdateBossInfo",
        new PacketDataSerializer(bossBarBuilder.buildPacket().serialize())));
    this.userBarSet.add(barType);
  }

  public void updateBossBar(final UserBarType barType, final String content) {
    this.sendPacket(
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateTitle(barType.getUniqueId())
                .title(TextComponent.fromLegacyText(content)).buildPacket().serialize()))
    );
  }

  public void updateBossBar(final UserBarType barType, final String content, final float progress) {
    this.sendPacket(
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateTitle(barType.getUniqueId())
                .title(TextComponent.fromLegacyText(content)).buildPacket().serialize())),
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateProgress(barType.getUniqueId()).progress(progress).buildPacket()
                .serialize()))
    );
  }

  public void updateBossBar(final UserBarType barType, final String content, final float progress,
      final BarColor barColor) {
    this.sendPacket(
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateTitle(barType.getUniqueId())
                .title(TextComponent.fromLegacyText(content)).buildPacket().serialize())),
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateProgress(barType.getUniqueId()).progress(progress).buildPacket()
                .serialize())),
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateStyle(barType.getUniqueId()).color(barColor).buildPacket()
                .serialize()))
    );
  }

  public void updateBossBar(final UserBarType barType, final String content, final float progress,
      final BarColor barColor, final BarStyle style) {
    this.sendPacket(
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateTitle(barType.getUniqueId())
                .title(TextComponent.fromLegacyText(content)).buildPacket().serialize())),
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateProgress(barType.getUniqueId()).progress(progress).buildPacket()
                .serialize())),
        new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
            BossBarBuilder.updateStyle(barType.getUniqueId()).color(barColor).style(style)
                .buildPacket()
                .serialize()))
    );
  }

  public void removeBossBar(final UserBarType barType) {
    if (this.userBarSet.remove(barType)) {
      this.sendPacket(new PacketPlayOutCustomPayload("BP|UpdateBossInfo", new PacketDataSerializer(
          BossBarBuilder.remove(barType.getUniqueId()).buildPacket().serialize())));
    }
  }

  public boolean hasBossBar(final UserBarType barType) {
    return this.userBarSet.contains(barType);
  }

  public void sendPacket(final Packet<?>... packets) {
    final Player player = Bukkit.getPlayer(this.uniqueId);
    if (player != null) {
      for (final Packet<?> packet : packets) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
      }
    }
  }

  public Set<UserBarType> getUserBarSet() {
    return userBarSet;
  }
}
