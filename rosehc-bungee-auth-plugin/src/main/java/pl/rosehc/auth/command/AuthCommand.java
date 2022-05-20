package pl.rosehc.auth.command;

import java.util.Map.Entry;
import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Name;
import me.vaperion.blade.annotation.Optional;
import me.vaperion.blade.annotation.Permission;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import net.md_5.bungee.api.CommandSender;
import org.mindrot.BCrypt;
import pl.rosehc.adapter.AdapterPlugin;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.auth.AuthPlugin;
import pl.rosehc.auth.mojang.MojangRequestHelper;
import pl.rosehc.auth.user.AuthUser;
import pl.rosehc.controller.packet.auth.user.AuthUserCreatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserDeletePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserPasswordUpdatePacket;
import pl.rosehc.controller.packet.auth.user.AuthUserSetPremiumStatePacket;
import pl.rosehc.controller.packet.platform.user.PlatformUserKickPacket;
import pl.rosehc.platform.PlatformPlugin;
import pl.rosehc.platform.user.PlatformUser;
import pl.rosehc.sectors.SectorsPlugin;
import pl.rosehc.sectors.sector.user.SectorUser;

public final class AuthCommand {

  private final AuthPlugin plugin;

  public AuthCommand(final AuthPlugin plugin) {
    this.plugin = plugin;
  }

  private static String getLastSectorName(final String nickname) {
    final java.util.Optional<PlatformUser> platformUserOptional = PlatformPlugin.getInstance()
        .getPlatformUserFactory().findUserByNickname(nickname);
    if (!platformUserOptional.isPresent()) {
      return "Brak";
    }

    final PlatformUser user = platformUserOptional.get();
    final String lastSectorName = AdapterPlugin.getInstance().getRedisAdapter()
        .get("rhc_player_sectors", user.getUniqueId().toString());
    return lastSectorName != null ? lastSectorName : "Brak";
  }

  @Permission("auth-command-register")
  @Command(value = "auth register", description = "Rejestruje konto dla podanego gracza.", async = true)
  public void handleRegister(final @Sender CommandSender sender,
      final @Name("nickname") String nickname, final @Name("password") String password) {
    if (this.plugin.getAuthUserFactory().findUser(nickname).isPresent()) {
      throw new BladeExitMessage(
          this.plugin.getAuthConfiguration().userAccountIsAlreadyRegistered.replace("{PLAYER_NAME}",
              nickname));
    }

    final Entry<String, Boolean> entry = MojangRequestHelper.fetchEntry(nickname);
    final AuthUser user = new AuthUser(entry.getKey(), null, System.currentTimeMillis(),
        System.currentTimeMillis(), entry.getValue());
    user.setPassword(!entry.getValue() ? BCrypt.hashpw(password, BCrypt.gensalt()) : null);
    user.setRegistered(true);
    ChatHelper.sendMessage(sender,
        this.plugin.getAuthConfiguration().successfullyRegisteredUser.replace("{PLAYER_NAME}",
            user.getNickname()));
    this.plugin.getAuthUserFactory().addUser(user);
    this.plugin.getRedisAdapter().sendPacket(
        new AuthUserCreatePacket(user.getNickname(), user.getPassword(), user.getLastIP(),
            user.getFirstJoinTime(), user.getLastOnlineTime(), user.isPremium(),
            user.isRegistered()), "rhc_master_controller", "rhc_auth");
  }

  @Permission("auth-command-changepassword")
  @Command(value = "auth info", description = "Wyświetla informację o koncie podanego gracza.", async = true)
  public void handleInfo(final @Sender CommandSender sender,
      final @Name("nickname") AuthUser user) {
    final java.util.Optional<SectorUser> sectorUserOptional = SectorsPlugin.getInstance()
        .getSectorUserFactory().findUserByNickname(user.getNickname());
    ChatHelper.sendMessage(sender,
        this.plugin.getAuthConfiguration().authUserInfo.replace("{PLAYER_NAME}", user.getNickname())
            .replace("{IP_ADDRESS}", user.getLastIP())
            .replace("{FIRST_JOIN_DATE}", TimeHelper.dateToString(user.getFirstJoinTime()))
            .replace("{LAST_ONLINE_TIME}", !sectorUserOptional.isPresent() ?
                TimeHelper.timeToString(System.currentTimeMillis() - user.getLastOnlineTime())
                    + " temu" : "teraz")
            .replace("{IS_ACTIVE}", sectorUserOptional.isPresent() ? "tak" : "nie")
            .replace("{LAST_SECTOR_NAME}",
                sectorUserOptional.isPresent() ? sectorUserOptional.get().getSector().getName()
                    : getLastSectorName(user.getNickname())));
  }

  @Permission("auth-command-setpremium")
  @Command(value = "auth setpremium", description = "Ustawia status konta gracza na premium/nonpremium.")
  public void handleSetPremium(final @Sender CommandSender sender,
      final @Name("nickname") AuthUser user,
      final @Name("state") @Optional(value = "true") boolean premium) {
    user.setPremium(premium);
    ChatHelper.sendMessage(sender, (!user.isPremium()
        ? this.plugin.getAuthConfiguration().successfullyChangedUserStatusToNonPremium
        : this.plugin.getAuthConfiguration().successfullyChangedUserStatusToPremium).replace(
        "{PLAYER_NAME}", user.getNickname()));
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserSetPremiumStatePacket(user.getNickname(), user.isPremium()),
            "rhc_master_controller", "rhc_auth");
    this.kickIfNeeded(user, !user.isPremium()
        ? this.plugin.getAuthConfiguration().yourAccountStatusHasBeenChangedToNonPremium
        : this.plugin.getAuthConfiguration().yourAccountStatusHasBeenChangedToPremium);
  }

  @Permission("auth-command-unregister")
  @Command(value = "auth unregister", description = "Odrejestrowuje konto podanego gracza.")
  public void handleUnregister(final @Sender CommandSender sender,
      final @Name("nickname") AuthUser user) {
    ChatHelper.sendMessage(sender,
        this.plugin.getAuthConfiguration().successfullyUnregisteredUser.replace("{PLAYER_NAME}",
            user.getNickname()));
    this.plugin.getAuthUserFactory().removeUser(user);
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserDeletePacket(user.getNickname()), "rhc_master_controller",
            "rhc_auth");
    this.kickIfNeeded(user,
        this.plugin.getAuthConfiguration().yourAccountHaveBeenUnregistered.replace("{PLAYER_NAME}",
            sender.getName()));
  }

  @Permission("auth-command-changepassword")
  @Command(value = "auth changepassword", description = "Zmienia hasło do konta podanego gracza.")
  public void handleChangePassword(final @Sender CommandSender sender,
      final @Name("nickname") AuthUser user, final @Name("password") String password) {
    user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
    ChatHelper.sendMessage(sender,
        this.plugin.getAuthConfiguration().successfullyChangedUserPassword.replace("{PLAYER_NAME}",
            sender.getName()));
    this.plugin.getRedisAdapter()
        .sendPacket(new AuthUserPasswordUpdatePacket(user.getNickname(), user.getPassword()),
            "rhc_master_controller", "rhc_auth");
  }

  @Permission("auth-command-auth")
  @Command(value = "auth", description = "Wyświetla użycie autha.")
  public void handleAuthUsage(final @Sender CommandSender sender) {
    ChatHelper.sendMessage(sender, this.plugin.getAuthConfiguration().authCommandUsage);
  }

  private void kickIfNeeded(final AuthUser user, final String kickMessage) {
    SectorsPlugin.getInstance().getSectorUserFactory().findUserByNickname(user.getNickname())
        .ifPresent(sectorUser -> this.plugin.getRedisAdapter()
            .sendPacket(new PlatformUserKickPacket(sectorUser.getUniqueId(), kickMessage),
                "rhc_platform_" + sectorUser.getProxy().getIdentifier()));
  }
}
