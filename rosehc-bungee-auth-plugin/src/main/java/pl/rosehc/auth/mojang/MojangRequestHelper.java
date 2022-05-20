package pl.rosehc.auth.mojang;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class MojangRequestHelper {

  private static final Cache<String, CompletableFuture<Entry<String, Boolean>>> CACHED_MOJANG_REQUESTS = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(1L)).build();
  private static final Cache<String, Entry<String, Boolean>> CACHED_MOJANG_ENTRIES = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(20L)).build();

  private MojangRequestHelper() {
  }

  public static Entry<String, Boolean> fetchEntry(String nickname) {
    final Entry<String, Boolean> entry = CACHED_MOJANG_ENTRIES.getIfPresent(nickname);
    if (Objects.nonNull(entry)) {
      return entry;
    }

    try {
      CompletableFuture<Entry<String, Boolean>> future = CACHED_MOJANG_REQUESTS.getIfPresent(
          nickname);
      if (Objects.isNull(future)) {
        future = tryFindDuplicate(nickname);
        if (Objects.isNull(future)) {
          future = new CompletableFuture<>();
          CACHED_MOJANG_REQUESTS.put(nickname, future);
        }
      }

      return future.get(10L, TimeUnit.SECONDS);
    } catch (final Exception ex) {
      throw new IllegalStateException("Cannot verify the user by nickname " + nickname + ".");
    }
  }

  public static Set<Entry<String, CompletableFuture<Entry<String, Boolean>>>> takeTenRequests() {
    return CACHED_MOJANG_REQUESTS.asMap().entrySet().stream().limit(10L)
        .collect(Collectors.toSet());
  }

  public static boolean fetchStatus(final String nickname) {
    return fetchEntry(nickname).getValue();
  }

  public static boolean isLimited() {
    return CACHED_MOJANG_REQUESTS.estimatedSize() >= 10;
  }

  public static void completeRequests(
      final Set<Entry<String, CompletableFuture<Entry<String, Boolean>>>> requests,
      final Map<String, Entry<String, Boolean>> statusMap) {
    requests.forEach(
        request -> request.getValue().complete(statusMap.get(request.getKey().toLowerCase())));
    CACHED_MOJANG_REQUESTS.asMap().entrySet().removeAll(requests);
  }

  public static void errorRequests() {
    CACHED_MOJANG_REQUESTS.asMap().values()
        .forEach(future -> future.completeExceptionally(new IllegalStateException()));
    CACHED_MOJANG_REQUESTS.cleanUp();
  }

  public static void cacheStatuses(final Map<String, Entry<String, Boolean>> statusMap) {
    CACHED_MOJANG_ENTRIES.putAll(statusMap);
  }

  private static CompletableFuture<Entry<String, Boolean>> tryFindDuplicate(final String nickname) {
    for (final Entry<String, CompletableFuture<Entry<String, Boolean>>> entry : CACHED_MOJANG_REQUESTS.asMap()
        .entrySet()) {
      if (entry.getKey().equalsIgnoreCase(nickname)) {
        return entry.getValue();
      }
    }

    return null;
  }
}
