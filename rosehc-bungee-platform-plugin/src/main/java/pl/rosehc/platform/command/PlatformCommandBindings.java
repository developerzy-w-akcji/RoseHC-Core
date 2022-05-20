package pl.rosehc.platform.command;

import java.util.ArrayList;
import java.util.List;
import me.vaperion.blade.argument.BladeArgument;
import me.vaperion.blade.argument.BladeProvider;
import me.vaperion.blade.bindings.Binding;
import me.vaperion.blade.context.BladeContext;
import me.vaperion.blade.exception.BladeExitMessage;
import me.vaperion.blade.service.BladeCommandService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class PlatformCommandBindings implements Binding {

  private final PlatformPlugin plugin;

  public PlatformCommandBindings(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void bind(final BladeCommandService service) {
    service.bindProvider(SectorUser.class, new BladeProvider<SectorUser>() {

      @Override
      public @NotNull List<String> suggest(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final List<String> completions = new ArrayList<>();
        final String input = argument.getString();
        if (input.trim().length() < 2) {
          return completions;
        }

        for (final SectorUser user : SectorsPlugin.getInstance().getSectorUserFactory().getUserMap()
            .values()) {
          if (user.getNickname().toLowerCase().startsWith(input)) {
            completions.add(user.getNickname());
          }
        }

        return completions;
      }

      @Override
      public @Nullable SectorUser provide(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final String input = argument.getString();
        return input != null && !input.equals("null") ? SectorsPlugin.getInstance()
            .getSectorUserFactory().findUserByNickname(input).orElseThrow(
                () -> new BladeExitMessage(ChatHelper.colored(
                    plugin.getPlatformConfiguration().messagesWrapper.playerIsOffline.replace(
                        "{PLAYER_NAME}", input)))) : null;
      }
    });
    service.bindProvider(PlatformUser.class, new BladeProvider<PlatformUser>() {

      @Override
      public @NotNull List<String> suggest(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final List<String> completions = new ArrayList<>();
        final String input = argument.getString();
        if (input.trim().length() < 2) {
          return completions;
        }

        for (final SectorUser user : SectorsPlugin.getInstance().getSectorUserFactory().getUserMap()
            .values()) {
          if (user.getNickname().toLowerCase().startsWith(input)) {
            completions.add(user.getNickname());
          }
        }

        return completions;
      }

      @Override
      public @Nullable PlatformUser provide(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final String input = argument.getString();
        return input != null && !input.equals("null") ? plugin.getPlatformUserFactory()
            .findUserByNickname(input).orElseThrow(() -> new BladeExitMessage(ChatHelper.colored(
                plugin.getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                    "{PLAYER_NAME}", input)))) : null;
      }
    });
  }
}
