package pl.rosehc.platform;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pl.rosehc.adapter.configuration.ConfigurationData;

public final class PlatformConfiguration extends ConfigurationData {

  @SerializedName("messages")
  public MessagesWrapper messagesWrapper = new MessagesWrapper();
  @SerializedName("ranks")
  public List<RankWrapper> rankList = Collections.singletonList(
      createRankWrapper("gracz", "&7", "&f", "&7", "&f", Collections.emptyList(), 1, true));
  @SerializedName("proxy_whitelist")
  public ProxyWhitelistWrapper proxyWhitelistWrapper = new ProxyWhitelistWrapper();
  @SerializedName("proxy_motd")
  public ProxyMotdWrapper proxyMotdWrapper = new ProxyMotdWrapper();
  @SerializedName("slots")
  public SlotWrapper slotWrapper = new SlotWrapper();

  private static RankWrapper createRankWrapper(final String name, final String chatPrefix,
      final String chatSuffix, final String nameTagPrefix, final String nameTagSuffix,
      final List<String> permissions, final int priority, final boolean defaultRank) {
    final RankWrapper wrapper = new RankWrapper();
    wrapper.name = name;
    wrapper.chatPrefix = chatPrefix;
    wrapper.chatSuffix = chatSuffix;
    wrapper.nameTagPrefix = nameTagPrefix;
    wrapper.nameTagSuffix = nameTagSuffix;
    wrapper.permissions = permissions;
    wrapper.priority = priority;
    wrapper.defaultRank = defaultRank;
    return wrapper;
  }

  public static final class MessagesWrapper {

    public String accountCreated = "&aTwoje konto zostało pomyślnie stworzone! Relognij.";
    public String proxyIsFull = "&cSerwer jest aktualnie pełen graczy!";
    public String proxyJoinIsCooldowned = "&cMusisz odczekać {TIME} przed następną próbą połączenia się z serwerem!";
    public String playerIsOffline = "&cGracz o nicku {PLAYER_NAME} jest aktualnie offline!";
    public String playerNotFound = "&cGracz o nicku {PLAYER_NAME} nie istnieje w bazie danych!";
    public String configReloadRequested = "&aProśba o przeładowanie configu została pomyślnie wysłana!";
    public String whitelistIsAlreadyEnabled = "&cWhitelista jest już włączona!";
    public String whitelistGotSuccessfullyEnabled = "&aPomyślnie włączono whitelistę na serwerach proxy!";
    public String whitelistIsAlreadyDisabled = "&cWhitelista jest już wyłączona!";
    public String whitelistGotSuccessfullyDisabled = "&aPomyślnie wyłączono whitelistę na serwerach proxy!";
    public String whitelistPlayerIsAlreadyWhitelisted = "&cPodany gracz jest już w whiteliście!";
    public String whitelistPlayerHasBeenSuccessfullyAdded = "&aPomyślnie dodano gracza {PLAYER_NAME} do whitelisty!";
    public String whitelistPlayerIsNotWhitelisted = "&cPodany gracz nie jest w whiteliście!";
    public String whitelistPlayerHasBeenSuccessfullyRemoved = "&aPomyślnie usunięto gracza {PLAYER_NAME} do whitelisty!";
    public String whitelistPlayerList = "&7Lista graczy w whiteliście: &5{PLAYERS}";
    public String whitelistReasonHasBeenSuccessfullyChanged = "&aPomyślnie zmieniono powód whitelisty na: &r{REASON}";
    public String whitelistUsage = "&5/whitelist add <nick> - &dDodaje gracza do whitelisty\n"
        + "&5/whitelist remove <nick> - &dUsuwa gracza z whitelisty\n"
        + "&5/whitelist on - &dWłącza whiteliste\n"
        + "&5/whitelist off - &dWyłącza whiteliste\n"
        + "&5/whitelist reason <powód> - &dUstawia powód whitelisty\n"
        + "&5/whitelist list - &dWyświetla listę osób w whiteliście";
    public String slotsUsage =
        "&5/setslots proxy <ilość> - &dUstawia sloty na podaną ilość na każdym proxy\n"
            + "&5/setslots sectors <ilość> - &dUstawia sloty na podaną ilość na każdym sektorze";
    public String slotsSuccessfullySet = "&aPomyślnie zmieniono sloty na {SLOTS}!";
    public String maskSuccessfullySet = "&aPomyślnie ustawiono maskę na {LIMIT}!";
    public String maskSuccessfullyDisabled = "&aPomyślnie wyłączono maskę!";
    public String sectorListInfo = "&5[{SECTOR_NAME}] &d{SECTOR_ONLINE_PLAYERS} &8- &r{FORMATTED_TPS} &8(&5{FORMATTED_LOAD}%&8)";
    public String proxyListInfo = "&5[{PROXY_IDENTIFIER}] &d{PROXY_ONLINE_PLAYERS} &8- &5{FORMATTED_LOAD}%";
    public String globalListInfo = "  &7Razem graczy &d{GLOBAL_ONLINE_PLAYERS} &8(&5{CURRENT_ONLINE_PLAYERS}&8)";
    public String motdLineSuccessfullyChanged = "&7Pomyślnie zmieniono linijkę motd &d{LINE_NUMBER} &7na: &r{LINE_TEXT}";
    public String successfullyKicked = "&7Pomyślnie wyrzuciłeś gracza o nicku &d{PLAYER_NAME} &7z powodem: &r{REASON}&7!";
    public String helpopIsCooldowned = "&cNie możesz wysyłać wiadomości na helpopie jeszcze przez {TIME}!";
    public String helpopMessageCannotBeEmpty = "&cWiadomość na helpopie nie może być pusta!";
    public String helpopMessageSuccessfullySent = "&aPomyślnie wysłałeś wiadomość do administracji!";
    public String helpopFormat = "&dHELPOP &8[&5proxy_{PROXY_IDENTIFIER}&8] [&5{SECTOR_NAME}&8] &d{PLAYER_NAME} &8-> &7{MESSAGE}";
    public String blazingPackIsOutdated = "&5Posiadasz nieaktualną wersję paczki!\n"
        + "&aJak pobrać nową wersję?\n"
        + "&7Wejdz na stronę: &dhttps://www.blazingpack.pl\n"
        + "&7Kliknij przycisk: &dPOBIERZ PACZKĘ/POBIERZ 1.8.8! (lub Pobierz automatyczny instalator (Windows only) na dole strony)\n"
        + "&7Odpakuj, przenieś do %appdata% -> .minecraft -> .versions, zrestartuj grę\n"
        + "&7i gotowe! Możesz cieszyć się ponownie grą na naszym serwerze :)";
    public String banUserIsAlreadyBanned = "&cTen użytkownik posiada już bana!";
    public String banUserIsNotBanned = "&cTen użytkownik nie posiada bana!";
    public String banCannotBeCreated = "&cNie można było stworzyć bana.";
    public String banCannotBeDeleted = "&cNie można było usunąć bana.";
    public String banPermBroadcastSilent = "&8[&cUKRYTY&8] &7Gracz &d{PLAYER_NAME} &7został permanentnie zbanowany przez administratora &d{STAFF_NAME} &7z powodem: &d{REASON}&7!";
    public String banPermBroadcastGlobal = "&7Gracz &d{PLAYER_NAME} &7został permanentnie zbanowany przez administratora &d{STAFF_NAME} &7z powodem: &d{REASON}&7!";
    public String banTempBroadcastSilent = "&8[&cUKRYTY&8] &7Gracz &d{PLAYER_NAME} &7został tymczasowo zbanowany przez administratora &d{STAFF_NAME} &7z powodem: &d{REASON} &7na czas: &d{TIME}&7!";
    public String banTempBroadcastGlobal = " &7Gracz &d{PLAYER_NAME} &7został tymczasowo zbanowany przez administratora &d{STAFF_NAME} &7z powodem: &d{REASON} &7na czas: &d{TIME}&7!";
    public String banUnBanBroadcastSilent = "&8[&cUKRYTY&8] &7Gracz &d{PLAYER_NAME} &7został odbanowany przez administratora &d{STAFF_NAME}&7!";
    public String banUnBanBroadcastGlobal = "&7Gracz &d{PLAYER_NAME} &7został odbanowany przez administratora &d{STAFF_NAME}&7!";
    public String banKickCommandPerm =
        "&cZostałeś permanentnie zbanowany przez administratora {STAFF_NAME}"
            + "\n&cRelognij, aby dowiedzieć się więcej!";
    public String banKickCommandTemp =
        "&cZostałeś tymczasowo zbanowany przez administratora {STAFF_NAME}"
            + "\n&cRelognij, aby dowiedzieć się więcej!";
    public String banKickJoinPerm = "&cZostałeś permanentnie przez administratora: &4{STAFF_NAME}"
        + "\n&cNick: &4{PLAYER_NAME}"
        + "\n&cPowód: &4{REASON}"
        + "\n&cBana utworzono: &4{CREATION_TIME}"
        + "\n&cNiesłuszny ban? Zgłoś się do nas na ts: &4ts.rosehc.pl &club dc: &4dc.rosehc.pl";
    public String banKickJoinTemp =
        "&cZostałeś tymczasowo zbanowany przez administratora: &4{STAFF_NAME}"
            + "\n&cNick: &4{PLAYER_NAME}"
            + "\n&cPowód: &4{REASON}"
            + "\n&cWygasa: &4za {LEFT_TIME}"
            + "\n&cBana utworzono: &4{CREATION_TIME}"
            + "\n&cNiesłuszny ban? Zgłoś się do nas na ts: &4ts.rosehc.pl &club dc: &4dc.rosehc.pl";
  }

  public static final class ProxyMotdWrapper {

    public String firstLine = "pierwsza", secondLine = "druga", thirdLine = "trzecia";
    public String playersInfo = "&7{ONLINE_PLAYERS}&8/&7{MAX_PLAYERS}";
    public List<String> hoverLines = Arrays.asList(
        "&7Strona: &dwww.rosehc.pl",
        "&7TS3: &dts.rosehc.pl",
        "&7Facebook: &dfb.rosehc.pl",
        "&7Discord: &ddc.rosehc.pl",
        "",
        "&7Proxy: &dproxy_{PROXY_IDENTIFIER}",
        "&7Użycie procesora: &d{PROXY_CPU_USAGE}",
        "&7Licznik nie pokaże więcej graczy niż: &d{COUNTER_PLAYERS_LIMIT}"
    );
    public int thirdLineSpacing = 24;
    public int counterPlayersLimit = 2000;
  }

  public static final class RankWrapper {

    public String name, chatPrefix, chatSuffix, nameTagPrefix, nameTagSuffix;
    public List<String> permissions;
    public int priority;
    public boolean defaultRank;
  }

  public static final class ProxyWhitelistWrapper {

    public List<String> players = new ArrayList<>(Arrays.asList("inzynierr", "squeru"));
    public String reason = "&cPrzerwa techniczna.";
    public boolean enabled = true;
  }

  public static final class SlotWrapper {

    @SerializedName("proxy_slots")
    public int proxySlots = 2000;
    @SerializedName("spigot_slots")
    public int spigotSlots = 500;
  }
}