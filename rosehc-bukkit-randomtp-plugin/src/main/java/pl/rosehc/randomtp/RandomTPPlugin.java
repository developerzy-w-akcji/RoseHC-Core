package pl.rosehc.randomtp;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import pl.rosehc.adapter.configuration.ConfigurationData;
import pl.rosehc.adapter.helper.ConfigurationHelper;
import pl.rosehc.adapter.plugin.BukkitPlugin;
import pl.rosehc.randomtp.linker.LinkerRandomTPPlugin;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;
import pl.rosehc.sectors.SectorsPlugin;

public final class RandomTPPlugin extends BukkitPlugin {

  private AbstractRandomTPPlugin pluginInstance;

  @Override
  public void onLoad() {
    final CurrentRandomTPConfiguration currentRandomTPConfiguration = ConfigurationHelper.load(
        new File(this.getDataFolder(), "config.json"), CurrentRandomTPConfiguration.class);
    this.pluginInstance = currentRandomTPConfiguration.linker ? new LinkerRandomTPPlugin(this)
        : new SystemRandomTPPlugin(this);
    this.getLogger().log(Level.INFO,
        "Stworzono implementację: " + this.pluginInstance.getClass().getSimpleName());
    SectorsPlugin.getInstance().registerHook(this.pluginInstance);
  }

  @Override
  public void onDisable() {
    if (Objects.nonNull(this.pluginInstance)) {
      this.pluginInstance.onDeInitialize();
    }
  }

  public static class CurrentRandomTPConfiguration extends ConfigurationData {

    public boolean linker = true;
  }
}
