package pl.rosehc.randomtp.system;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import pl.rosehc.adapter.configuration.ConfigurationData;

public final class SystemRandomTPConfiguration extends ConfigurationData {

  public String arenaDeletionActionBarInfo = "&7Arena zostanie usunięta za: &d{TIME}";
  public String cannotConnectToSector = "&cNie można było połączyć się z grywalnym sektorem...";
  public String cannotExecuteThisCommandOnThisSector = "&cNie możesz wykonywać tej komendy na tym sektorze!";
  public String creationIdleTime = "5s", deletionTime = "30s";
  @SerializedName("allowed_commands")
  public List<String> allowedCommandList = Arrays.asList(
      "/msg", "/tell",
      "/m", "/reply",
      "/r", "/pomoc",
      "/spawn", "/teleport",
      "/tp"
  );
  public int cuboidSize = 100, cuboidMinDistance = 10;
  public transient long parsedCreationIdleTime, parsedDeletionTime;
}
