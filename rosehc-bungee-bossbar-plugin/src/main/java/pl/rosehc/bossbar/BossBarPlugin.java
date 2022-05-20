package pl.rosehc.bossbar;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * @author stevimeister on 03/01/2022
 **/
public final class BossBarPlugin extends Plugin implements Listener {

  private static final String TAG = "BP|UpdateBossInfo";
  private final Multimap<UUID, UUID> sentBossBars = Multimaps.synchronizedMultimap(
      HashMultimap.create());

  @Override
  public void onEnable() {
    this.getProxy().getPluginManager().registerListener(this, this);
  }

  @EventHandler
  public void onPluginMessage(final PluginMessageEvent event) {
    if (!event.getTag().equals(TAG) || !(event.getReceiver() instanceof ProxiedPlayer)) {
      return;
    }

    final ByteBuf dataBuffer = Unpooled.wrappedBuffer(event.getData());
    final UUID playerUniqueID = ((ProxiedPlayer) event.getReceiver()).getUniqueId();
    final UUID barUniqueID = DefinedPacket.readUUID(dataBuffer);
    switch (DefinedPacket.readVarInt(dataBuffer)) {
      case 0: {
        this.sentBossBars.put(playerUniqueID, barUniqueID);
        break;
      }

      case 1: {
        this.sentBossBars.remove(playerUniqueID, barUniqueID);
      }
    }
  }

  @EventHandler
  public void onServerConnected(final ServerConnectedEvent event) {
    final ProxiedPlayer player = event.getPlayer();
    for (final UUID uniqueId : this.sentBossBars.removeAll(player.getUniqueId())) {
      player.unsafe().sendPacket(new PluginMessage(TAG, this.remove(uniqueId), true));
    }
  }

  private byte[] remove(final UUID barUUID) {
    final ByteBuf removedBuffer = Unpooled.buffer();
    DefinedPacket.writeUUID(barUUID, removedBuffer);
    DefinedPacket.writeVarInt(1, removedBuffer);
    final byte[] removedData = new byte[removedBuffer.readableBytes()];
    removedBuffer.readBytes(removedData);
    return removedData;
  }
}
