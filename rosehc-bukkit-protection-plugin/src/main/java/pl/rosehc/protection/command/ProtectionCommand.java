package pl.rosehc.protection.command;

import java.sql.SQLException;
import java.util.logging.Level;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.protection.ProtectionPlugin;

public final class ProtectionCommand {

  private final ProtectionPlugin plugin;

  public ProtectionCommand(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"protection",
      "ochrona"}, description = "Komenda od wyłączania ochrony gracza", async = true)
  public void handleProtection(final @Sender Player player) {
    this.plugin.getProtectionUserFactory().findUser(player).ifPresent(user -> {
      if (user.hasExpired()) {
        ChatHelper.sendMessage(player,
            this.plugin.getProtectionConfiguration().protectionIsAlreadyDisabled);
        return;
      }

      user.setExpiryTime(0L);
      ChatHelper.sendMessage(player,
          this.plugin.getProtectionConfiguration().successfullyDisabledYourProtection);
      try {
        this.plugin.getProtectionUserRepository().update(user);
      } catch (final SQLException ex) {
        this.plugin.getLogger()
            .log(Level.SEVERE, "Nie można było wykonać update'u użytkownika.", ex);
      }
    });
  }
}
