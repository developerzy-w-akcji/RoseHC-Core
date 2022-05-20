package pl.rosehc.guilds.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import me.vaperion.blade.argument.BladeArgument;
import me.vaperion.blade.argument.BladeProvider;
import me.vaperion.blade.bindings.Binding;
import me.vaperion.blade.context.BladeContext;
import me.vaperion.blade.exception.BladeExitMessage;
import me.vaperion.blade.service.BladeCommandService;
import org.jetbrains.annotations.NotNull;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class GuildCommandBindings implements Binding {

  private final GuildsPlugin plugin;

  public GuildCommandBindings(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  private static List<String> provideUserSuggestions(final BladeContext context,
      final String input) {
    final List<String> completions = new ArrayList<>();
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
  public void bind(final BladeCommandService service) {
    final BiFunction<BladeContext, String, List<String>> suggestionsProvider = GuildCommandBindings::provideUserSuggestions;
    service.bindProvider(Guild.class, new BladeProvider<Guild>() {

      @Override
      public @NotNull List<String> suggest(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final List<String> completions = new ArrayList<>();
        final String input = argument.getString().toLowerCase();
        if (input.trim().length() < 3) {
          return completions;
        }

        for (final Guild guild : plugin.getGuildFactory().getGuildMap().values()) {
          if (guild.getTag().toLowerCase().startsWith(input)) {
            completions.add(guild.getTag());
          }
        }

        return completions;
      }

      @Override
      public Guild provide(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final String input = argument.getString();
        return Objects.nonNull(input) && !input.equals("null") ? plugin.getGuildFactory()
            .findGuildByCredential(input, true).orElseThrow(() -> new BladeExitMessage(
                ChatHelper.colored(
                    plugin.getGuildsConfiguration().messagesWrapper.guildNotFound.replace("{TAG}",
                        input)))) : null;
      }
    });
    service.bindProvider(GuildUser.class, new BladeProvider<GuildUser>() {

      @Override
      public GuildUser provide(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final String input = argument.getString();
        return Objects.nonNull(input) && !input.equals("null") ? plugin.getGuildUserFactory()
            .findUserByNickname(input).orElseThrow(() -> new BladeExitMessage(ChatHelper.colored(
                PlatformPlugin.getInstance()
                    .getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                        "{PLAYER_NAME}", input)))) : null;
      }

      @Override
      public @NotNull List<String> suggest(final @NotNull BladeContext context,
          final @NotNull BladeArgument argument) throws BladeExitMessage {
        final String input = argument.getString().toLowerCase();
        return suggestionsProvider.apply(context, input);
      }
    });
  }
}
