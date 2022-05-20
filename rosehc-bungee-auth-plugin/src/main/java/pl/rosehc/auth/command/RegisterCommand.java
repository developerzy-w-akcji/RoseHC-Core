package pl.rosehc.auth.command;

import static pl.rosehc.auth.command.AuthCommandBindings.ensureIsAuthenticated;
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
import pl.rosehc.controller.packet.auth.user.AuthUserMarkRegisteredPacket;
import pl.rosehc.controller.packet.auth.user.AuthUserPasswordUpdatePacket;

public final class RegisterCommand {

  private final AuthPlugin plugin;

  public RegisterCommand(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"register",
      "reg"}, description = "Wykonuje zarejestrowania nowego konta dla gracza", async = true)
  public void handleRegister(final @Sender ProxiedPlayer player,
      final @Name("password") String password,
      final @Name("confirm_password") String confirmationPassword) {
    final AuthUser user = this.plugin.getAuthUserFactory().findUser(player.getName()).orElseThrow(
        () -> new BladeExitMessage(
            ChatHelper.colored(this.plugin.getAuthConfiguration().userDataNotFound)));
    ensureNotPremium(player, user);
    ensureIsAuthenticated(player, user);
    if (user.isRegistered()) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().alreadyRegistered));
    }

    if (password.length() < 3) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordIsTooShort));
    }

    if (!password.equals(confirmationPassword)) {
      throw new BladeExitMessage(
          ChatHelper.colored(this.plugin.getAuthConfiguration().passwordsAreNotTheSame));
    }

    user.setRegistered(true);
    user.setLogged(true);
    user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
    ChatHelper.sendMessage(player, this.plugin.getAuthConfiguration().successfullyRegistered);
    AuthUserQueueHelper.addToQueue(player);
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserMarkRegisteredPacket(user.getNickname()), "rhc_master_controller",
            "rhc_platform");
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserPasswordUpdatePacket(user.getNickname(), user.getPassword()),
            "rhc_master_controller", "rhc_platform");
  }
}
