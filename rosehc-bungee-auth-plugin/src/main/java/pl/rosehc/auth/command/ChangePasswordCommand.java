package pl.rosehc.auth.command;

import static pl.rosehc.auth.command.AuthCommandBindings.ensureIsAuthenticated;
import static pl.rosehc.auth.command.AuthCommandBindings.ensureIsRegistered;
import static pl.rosehc.auth.command.AuthCommandBindings.ensureNotPremium;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mindrot.BCrypt;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.controller.packet.auth.user.AuthUserPasswordUpdatePacket;

public final class ChangePasswordCommand {

  private final AuthPlugin plugin;

  public ChangePasswordCommand(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"changepassword", "zmienhaslo",
      "cp"}, description = "Zmienia hasło do konta gracza", async = true)
  public void handleChangePassword(final @Sender ProxiedPlayer player,
      final @Name("old_password") String oldPassword,
      final @Name("new_password") String newPassword) {
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(player.getName()).orElseThrow(
        () -> new BladeExitMessage(
            ChatHelper.colored(this.plugin.getAuthConfiguration().userDataNotFound)));
    ensureNotPremium(player, user);
    ensureIsRegistered(player, user);
    ensureIsAuthenticated(player, user);
    if (oldPassword.equals(newPassword)) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordsAreTheSame));
    }

    if (newPassword.length() < 3) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordIsTooShort));
    }

    if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordDidntMatch));
    }

    user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    ChatHelper.sendMessage(player,
        this.plugin.getAuthConfiguration().successfullyChangedYourPassword);
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserPasswordUpdatePacket(user.getNickname(), user.getPassword()),
            "rhc_master_controller", "rhc_platform");
  }
}
