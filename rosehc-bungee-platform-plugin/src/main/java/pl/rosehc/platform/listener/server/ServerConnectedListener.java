package pl.rosehc.platform.listener.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

public final class ServerConnectedListener implements Listener {

  @EventHandler
  public void onServerConnected(final ServerConnectedEvent event) {
    event.getPlayer().unsafe().sendPacket(new PluginMessage("bp:waypoint", this.removeAll(), true));
  }

  private byte[] removeAll() {
    final ByteBuf removedBuffer = Unpooled.buffer();
    removedBuffer.writeByte(2);
    final byte[] removedData = new byte[removedBuffer.readableBytes()];
    removedBuffer.readBytes(removedData);
    return removedData;
  }
}
