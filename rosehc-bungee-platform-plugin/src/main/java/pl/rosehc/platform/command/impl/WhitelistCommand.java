package pl.rosehc.platform.command.impl;

import me.vaperion.blade.annotation.Combined;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import net.md_5.bungee.api.CommandSender;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistChangeStatePacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistSetReasonPacket;
import pl.rosehc.controller.packet.platform.whitelist.PlatformWhitelistUpdatePlayerPacket;
import pl.rosehc.platform.PlatformPlugin;

public final class WhitelistCommand {

  private final PlatformPlugin plugin;

  public WhitelistCommand(final PlatformPlugin plugin) {
    this.plugin = plugin;
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist add",
      "wl add"}, async = true, description = "Dodaje gracza do whitelisty.")
  public void handleWhitelistAdd(final @Sender CommandSender sender,
      final @Name("nick") String playerName) {
    if (this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.contains(playerName)) {
      ChatHelper.sendMessage(sender,
          this.plugin.getPlatformConfiguration().messagesWrapper.whitelistPlayerIsAlreadyWhitelisted);
      return;
    }

    this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.add(playerName);
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformWhitelistUpdatePlayerPacket(playerName, true),
            "rhc_master_controller", "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistPlayerHasBeenSuccessfullyAdded.replace(
            "{PLAYER_NAME}", playerName));
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist remove",
      "wl remove"}, async = true, description = "Usuwa gracza z whitelisty.")
  public void handleWhitelistRemove(final @Sender CommandSender sender,
      final @Name("nick") String playerName) {
    if (!this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.contains(
        playerName)) {
      ChatHelper.sendMessage(sender,
          this.plugin.getPlatformConfiguration().messagesWrapper.whitelistPlayerIsNotWhitelisted);
      return;
    }

    this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.remove(playerName);
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformWhitelistUpdatePlayerPacket(playerName, false),
            "rhc_master_controller", "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistPlayerHasBeenSuccessfullyRemoved.replace(
            "{PLAYER_NAME}", playerName));
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist on", "wl on"}, async = true, description = "Włącza whitelistę.")
  public void handleWhitelistEnable(final @Sender CommandSender sender) {
    if (this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.enabled) {
      ChatHelper.sendMessage(sender,
          this.plugin.getPlatformConfiguration().messagesWrapper.whitelistIsAlreadyEnabled);
      return;
    }

    this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.enabled = true;
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformWhitelistChangeStatePacket(true), "rhc_master_controller",
            "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistGotSuccessfullyEnabled);
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist off", "wl off"}, async = true, description = "Wyłącza whitelistę.")
  public void handleWhitelistDisable(final @Sender CommandSender sender) {
    if (!this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.enabled) {
      ChatHelper.sendMessage(sender,
          this.plugin.getPlatformConfiguration().messagesWrapper.whitelistIsAlreadyDisabled);
      return;
    }

    this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.enabled = false;
    this.plugin.getRedisAdapter()
        .sendPacket(new PlatformWhitelistChangeStatePacket(false), "rhc_master_controller",
            "rhc_platform");
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistGotSuccessfullyDisabled);
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist reason",
      "wl reason"}, async = true, description = "Ustawia powód whitelisty.")
  public void handleWhitelistReason(final @Sender CommandSender sender,
      final @Name("powód") @Combined String reason) {
    this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.reason = reason;
    this.plugin.getRedisAdapter().sendPacket(new PlatformWhitelistSetReasonPacket(reason));
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistReasonHasBeenSuccessfullyChanged.replace(
            "{REASON}", reason));
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist list", "wl list"}, description = "Wyświetla użycie whitelisty.")
  public void handleWhitelistList(final @Sender CommandSender sender) {
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistPlayerList.replace(
            "{PLAYERS}",
            !this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players.isEmpty()
                ? String.join(", ",
                this.plugin.getPlatformConfiguration().proxyWhitelistWrapper.players) : "Brak."));
  }

  @Permission("platform-command-whitelist")
  @Command(value = {"whitelist", "wl"}, description = "Wyświetla użycie whitelisty.")
  public void handleWhitelistUsage(final @Sender CommandSender sender) {
    ChatHelper.sendMessage(sender,
        this.plugin.getPlatformConfiguration().messagesWrapper.whitelistUsage);
  }
}
