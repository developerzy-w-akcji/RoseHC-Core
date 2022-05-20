package pl.rosehc.auth.mojang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import pl.rosehc.auth.AuthPlugin;

public final class MojangRequestTask implements Runnable {

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  public MojangRequestTask(final AuthPlugin plugin) {
    plugin.getProxy().getScheduler().schedule(plugin, this, 2L, 2L, TimeUnit.SECONDS);
  }

  private static void requestNormal(
      final Set<Entry<String, CompletableFuture<Entry<String, Boolean>>>> requests)
      throws IOException {
    final HttpURLConnection connection = (HttpURLConnection) new URL(
        "https://api.mojang.com/profiles/minecraft").openConnection();
    try {
      final Map<String, Boolean> statusMap = new HashMap<>();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoInput(true);
      connection.setDoOutput(true);
      try (final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
        final JsonArray array = new JsonArray();
        requests.forEach(entry -> array.add(new JsonPrimitive(entry.getKey())));
        writer.write(array.toString());
      }

      try (final BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream()))) {
        final JsonArray array = GSON.fromJson(reader.lines().collect(Collectors.joining()),
            JsonArray.class);
        for (final JsonElement element : array) {
          if (!element.isJsonObject()) {
            continue;
          }

          final JsonObject object = element.getAsJsonObject();
          if (!object.has("name")) {
            throw new IOException("Cannot verify the requests. (NO NICKNAME)");
          }

          statusMap.put(object.get("name").getAsString(), true);
        }
      }

      for (final Entry<String, CompletableFuture<Entry<String, Boolean>>> request : requests) {
        if (!statusMap.containsKey(request.getKey())) {
          statusMap.put(request.getKey(), false);
        }
      }

      final Map<String, Entry<String, Boolean>> fixedStatusMap = statusMap.entrySet().stream().map(
              entry -> new SimpleEntry<>(entry.getKey().toLowerCase(),
                  new SimpleEntry<>(entry.getKey(), entry.getValue())))
          .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
      MojangRequestHelper.completeRequests(requests, fixedStatusMap);
      MojangRequestHelper.cacheStatuses(fixedStatusMap);
    } finally {
      connection.disconnect();
    }
  }

  private static void requestFailover(
      final Set<Entry<String, CompletableFuture<Entry<String, Boolean>>>> requests)
      throws IOException {
    final Map<String, Boolean> statusMap = new HashMap<>();
    for (final Entry<String, CompletableFuture<Entry<String, Boolean>>> entry : requests) {
      final HttpURLConnection connection = (HttpURLConnection) new URL(
          "https://api.minetools.eu/uuid/" + entry.getKey()).openConnection();
      try {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        try (final BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream()))) {
          final JsonObject object = GSON.fromJson(reader.lines().collect(Collectors.joining()),
              JsonObject.class);
          if (!object.has("status")) {
            throw new IOException("Cannot verify the requests. (NO STATUS)");
          }

          final String status = object.get("status").getAsString();
          if (!status.equals("OK") && !status.equals("ERR")) {
            throw new IOException("Cannot verify the requests. (NO OK/ERR STATUS)");
          }

          if (!object.has("name")) {
            throw new IOException("Cannot verify the requests. (NO NICKNAME)");
          }

          statusMap.put(object.get("name").getAsString(), status.equals("OK"));
        }
      } finally {
        connection.disconnect();
      }
    }

    final Map<String, Entry<String, Boolean>> fixedStatusMap = statusMap.entrySet().stream().map(
            entry -> new SimpleEntry<>(entry.getKey().toLowerCase(),
                new SimpleEntry<>(entry.getKey(), entry.getValue())))
        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    MojangRequestHelper.completeRequests(requests, fixedStatusMap);
    MojangRequestHelper.cacheStatuses(fixedStatusMap);
  }

  @Override
  public void run() {
    final Set<Entry<String, CompletableFuture<Entry<String, Boolean>>>> requests = MojangRequestHelper.takeTenRequests();
    if (!requests.isEmpty()) {
      try {
        // normal
        requestNormal(requests);
      } catch (final IOException e) {
        // failover
        try {
          requestFailover(requests);
        } catch (final IOException ex) {
          MojangRequestHelper.errorRequests();
          AuthPlugin.getInstance().getLogger()
              .log(Level.SEVERE, "Nie można było zweryfikować kont.", ex);
        }
      }
    }
  }
}
