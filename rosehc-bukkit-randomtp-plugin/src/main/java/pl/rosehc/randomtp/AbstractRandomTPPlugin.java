package pl.rosehc.randomtp;

import org.bukkit.Server;
import pl.rosehc.adapter.redis.RedisAdapter;
import pl.rosehc.sectors.sector.SectorInitializationHook;

public abstract class AbstractRandomTPPlugin implements SectorInitializationHook {

  protected final RandomTPPlugin original;

  public AbstractRandomTPPlugin(final RandomTPPlugin original) {
    this.original = original;
  }

  public RandomTPPlugin getOriginal() {
    return this.original;
  }

  public RedisAdapter getRedisAdapter() {
    return this.original.getRedisAdapter();
  }

  public Server getServer() {
    return this.original.getServer();
  }

  public abstract void onDeInitialize();
}
