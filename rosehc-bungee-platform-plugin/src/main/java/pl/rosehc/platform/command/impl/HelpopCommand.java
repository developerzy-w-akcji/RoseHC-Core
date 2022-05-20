package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.platform.user.PlatformUserSendHelpopMessagePacket;
import pl.rosehc.controller.wrapper.platform.PlatformUserCooldownType;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.Sector;
import pl.rosehc.sectors.sector.user.SectorUser;

@SuppressWarnings("SpellCheckingInspection")
public final class HelpopCommand {

  private final PlatformPlugin plugin;

  public HelpopCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = "helpop", description = "Wysyła wiadomość do administratorów")
  public void handleHelpop(final @Sender ProxiedPlayer player,
      @Name("message") @Combined String message) {
    message = ChatColor.stripColor(ChatHelper.colored(message));
    if (message.trim().isEmpty()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.helpopMessageCannotBeEmpty));
    }

    final PlatformUser user = this.plugin.getPlatformUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            this.plugin.getPlatformConfiguration().messagesWrapper.playerNotFound.replace(
                "{PLAYER_NAME}", player.getName())));
    if (user.getCooldownCache().hasUserCooldown(PlatformUserCooldownType.HELPOP)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getPlatformConfiguration().messagesWrapper.helpopIsCooldowned.replace(
              "{TIME}", TimeHelper.timeToString(
                  user.getCooldownCache().getUserCooldown(PlatformUserCooldownType.HELPOP)))));
    }

    ChatHelper.sendMessage(player,
        this.plugin.getPlatformConfiguration().messagesWrapper.helpopMessageSuccessfullySent);
    user.getCooldownCache().putUserCooldown(PlatformUserCooldownType.HELPOP);
    this.plugin.getRedisAdapter().sendPacket(
        new PlatformUserSendHelpopMessagePacket(player.getName(),
            SectorsPlugin.getInstance().getSectorUserFactory()
                .findUserByUniqueId(player.getUniqueId()).map(SectorUser::getSector)
                .map(Sector::getName).orElse("N/A"), message,
            SectorsPlugin.getInstance().getProxyFactory().getCurrentProxy().getIdentifier()),
        "rhc_platform");
  }
}
