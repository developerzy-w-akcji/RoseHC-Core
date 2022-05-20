package pl.rosehc.platform.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import pl.blazingpack.bpauth.BlazingPackAuthEvent;

public final class PlatformUserBlazingAuthCancellable {

  private static final Cache<UUID, AtomicInteger> CANCELLED_BP_AUTHS = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(1L)).build();

  public static boolean isCancelled(final BlazingPackAuthEvent event) {
    final AtomicInteger count = CANCELLED_BP_AUTHS.getIfPresent(
        event.getUserConnection().getUniqueId());
    if (Objects.nonNull(count)) {
      if (count.decrementAndGet() == 0) {
        CANCELLED_BP_AUTHS.invalidate(event.getUserConnection().getUniqueId());
      }
      return true;
    }

    return false;
  }

  public static void cancel(final BlazingPackAuthEvent event) {
    AtomicInteger count = CANCELLED_BP_AUTHS.getIfPresent(event.getUserConnection().getUniqueId());
    if (Objects.isNull(count)) {
      count = new AtomicInteger();
      CANCELLED_BP_AUTHS.put(event.getUserConnection().getUniqueId(), count);
    }

    count.incrementAndGet();
  }
}
