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
import pl.rosehc.auth.user.AuthUserQueueHelper;

public final class LoginCommand {

  private final AuthPlugin plugin;

  public LoginCommand(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"login",
      "l"}, description = "Wykonuje zalogowanie gracza na jego konto", async = true)
  public void handleLogin(final @Sender ProxiedPlayer player,
      final @Name("password") String password) {
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(player.getName()).orElseThrow(
        () -> new BladeExitMessage(
            ChatHelper.colored(this.plugin.getAuthConfiguration().userDataNotFound)));
    ensureNotPremium(player, user);
    ensureIsRegistered(player, user);
    ensureIsAuthenticated(player, user);
    if (user.isLogged()) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().alreadyLogged));
    }

    if (!BCrypt.checkpw(password, user.getPassword())) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordDidntMatch));
    }

    user.setLogged(true);
    ChatHelper.sendMessage(player, this.plugin.getAuthConfiguration().successfullyLoggedIn);
    AuthUserQueueHelper.addToQueue(player);
  }
}
