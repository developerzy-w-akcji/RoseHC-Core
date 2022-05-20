package pl.rosehc.guilds.tablist;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.guilds.GuildsPlugin;

public final class TabList {

  private static final Constructor<?> PLAYER_INFO_CONSTRUCTOR;
  private static final Field PROFILE_LIST_FIELD, FOOTER_FIELD;

  static {
    try {
      PLAYER_INFO_CONSTRUCTOR = PlayerInfoData.class.getDeclaredConstructors()[0];
      PLAYER_INFO_CONSTRUCTOR.setAccessible(true);
      PROFILE_LIST_FIELD = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
      PROFILE_LIST_FIELD.setAccessible(true);
      FOOTER_FIELD = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("b");
      FOOTER_FIELD.setAccessible(true);
    } catch (final Exception ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  private final Player player;
  private final GameProfile[] profiles;
  private int currentElementsIndex;
  private long lastElementsUpdate;
  private boolean elementsInitialized;

  public TabList(final Player player) {
    this.player = player;
    this.profiles = new GameProfile[80];
  }

  public void update() {
    if (!this.player.isOnline()) {
      return;
    }

    try {
      final TabListElements elements = this.fetchOrUpdateElements();
      final List<PlayerInfoData> addedDataList = new ArrayList<>(), updatedDataList = new ArrayList<>();
      final PacketPlayOutPlayerInfo addPlayerPacket = new PacketPlayOutPlayerInfo(
          EnumPlayerInfoAction.ADD_PLAYER), updatePlayerPacket = new PacketPlayOutPlayerInfo(
          EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
      for (int index = 0; index < this.profiles.length; index++) {
        GameProfile profile = this.profiles[index];
        boolean newlyCreated = false;
        if (profile == null) {
          profile = new GameProfile(UUID.randomUUID(), String.format("!!UPDATEMC%02d", index));
          newlyCreated = true;
          this.profiles[index] = profile;
        }

        final PlayerInfoData data = (PlayerInfoData) PLAYER_INFO_CONSTRUCTOR.newInstance(
            newlyCreated ? addPlayerPacket : updatePlayerPacket,
            profile, 0, EnumGamemode.NOT_SET,
            new ChatComponentText(ChatHelper.colored(elements.getElement(this.player, index)))
        );
        if (newlyCreated) {
          addedDataList.add(data);
        } else {
          updatedDataList.add(data);
        }
      }

      final PlayerConnection connection = ((CraftPlayer) this.player).getHandle().playerConnection;
      if (!addedDataList.isEmpty()) {
        PROFILE_LIST_FIELD.set(addPlayerPacket, addedDataList);
        connection.sendPacket(addPlayerPacket);
      }
      if (!updatedDataList.isEmpty()) {
        PROFILE_LIST_FIELD.set(updatePlayerPacket, updatedDataList);
        connection.sendPacket(updatePlayerPacket);
      }

      final String header = elements.getHeader(this.player), footer = elements.getFooter(
          this.player);
      if (header != null && footer != null) {
        final PacketPlayOutPlayerListHeaderFooter headerFooterPacket = new PacketPlayOutPlayerListHeaderFooter(
            new ChatComponentText(ChatHelper.colored(header)));
        FOOTER_FIELD.set(headerFooterPacket, new ChatComponentText(ChatHelper.colored(footer)));
        connection.sendPacket(headerFooterPacket);
      }
    } catch (final Exception ex) {
      throw new UnsupportedOperationException(ex);
    }
  }

  private TabListElements fetchOrUpdateElements() {
    final TabListElements[] tabListElements = GuildsPlugin.getInstance().getTabListFactory()
        .getTabListElements();
    if (!this.elementsInitialized) {
      this.lastElementsUpdate = System.currentTimeMillis() + tabListElements[0].getUpdateTime();
      this.elementsInitialized = true;
      return tabListElements[0];
    }

    if (this.lastElementsUpdate <= System.currentTimeMillis()) {
      final TabListElements nextElements = tabListElements[this.currentElementsIndex++
          % tabListElements.length];
      this.lastElementsUpdate = System.currentTimeMillis() + nextElements.getUpdateTime();
      return nextElements;
    }

    return tabListElements[this.currentElementsIndex % tabListElements.length];
  }
}
