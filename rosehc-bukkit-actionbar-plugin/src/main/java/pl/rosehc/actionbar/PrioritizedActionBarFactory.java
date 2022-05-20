package pl.rosehc.actionbar;

import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PrioritizedActionBarFactory {

  private final Map<UUID, Queue<PrioritizedActionBar>> actionBarQueueMap = new ConcurrentHashMap<>();
  private final ReentrantLock queueOperationLock = new ReentrantLock();

  public void sendActionBars(final Player player) {
    final Queue<PrioritizedActionBar> queue = this.actionBarQueueMap.get(player.getUniqueId());
    if (Objects.isNull(queue)) {
      return;
    }

    this.queueOperationLock.lock();
    try {
      if (queue.removeIf(PrioritizedActionBar::hasTimedOut) && queue.isEmpty()) {
        this.actionBarQueueMap.remove(player.getUniqueId());
        return;
      }

      final PrioritizedActionBar actionBar = queue.peek();
      if (Objects.nonNull(actionBar)) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
            new PacketPlayOutChat(new ChatComponentText(actionBar.getText()), (byte) 2));
      }
    } finally {
      this.queueOperationLock.unlock();
    }
  }

  public void updateActionBar(final UUID uniqueId, final String text, final int priority) {
    final Queue<PrioritizedActionBar> queue = this.actionBarQueueMap.computeIfAbsent(uniqueId,
        ignored -> new PriorityQueue<>());
    this.queueOperationLock.lock();
    try {
      for (final PrioritizedActionBar actionBar : queue) {
        if (actionBar.getPriority() == priority) {
          actionBar.setText(text);
          break;
        }
      }

      queue.add(new PrioritizedActionBar(text, priority));
    } finally {
      this.queueOperationLock.unlock();
    }
  }

  public void removeActionBars(final UUID uniqueId) {
    final Queue<PrioritizedActionBar> queue = this.actionBarQueueMap.remove(uniqueId);
    if (Objects.nonNull(queue)) {
      this.queueOperationLock.lock();
      try {
        queue.clear();
      } finally {
        this.queueOperationLock.unlock();
      }
    }
  }
}
