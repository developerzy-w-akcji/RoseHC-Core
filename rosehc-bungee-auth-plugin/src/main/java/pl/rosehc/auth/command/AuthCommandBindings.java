package pl.rosehc.auth.command;

import java.util.Objects;
import me.vaperion.blade.bindings.Binding;
import me.vaperion.blade.exception.BladeExitMessage;
import me.vaperion.blade.service.BladeCommandService;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.user.AuthUser;

public final class AuthCommandBindings implements Binding {

  private final AuthPlugin plugin;

  public AuthCommandBindings(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  public static void ensureIsAuthenticated(final ProxiedPlayer player, final AuthUser user) {
    if (!user.isBlazingAuthenticated()) {
      final String blazingpackAuthenticationError = ChatHelper.colored(
          AuthPlugin.getInstance().getAuthConfiguration().blazingpackAuthenticationError);
      player.disconnect(blazingpackAuthenticationError);
      throw new BladeExitMessage(blazingpackAuthenticationError);
    }
  }

  public static void ensureNotPremium(final ProxiedPlayer player, final AuthUser user) {
    if (user.isPremium()) {
      throw new BladeExitMessage(ChatHelper.colored(
          AuthPlugin.getInstance().getAuthConfiguration().cannotExecuteThisCommandAsPremium));
    }
  }

  public static void ensureIsRegistered(final ProxiedPlayer player, final AuthUser user) {
    if (!user.isRegistered()) {
      throw new BladeExitMessage(
          ChatHelper.colored(AuthPlugin.getInstance().getAuthConfiguration().notRegistered));
    }
  }

  @Override
  public void bind(final BladeCommandService service) {
    service.bindProvider(AuthUser.class, (context, argument) -> {
      final String input = argument.getString();
      return Objects.nonNull(input) && !input.equals("null") ? this.plugin.getAuthUserFactory()
          .findUser(input).orElseThrow(() -> new BladeExitMessage(ChatHelper.colored(
              this.plugin.getAuthConfiguration().userNotFound.replace("{PLAYER_NAME}", input))))
          : null;
    });
  }
}
