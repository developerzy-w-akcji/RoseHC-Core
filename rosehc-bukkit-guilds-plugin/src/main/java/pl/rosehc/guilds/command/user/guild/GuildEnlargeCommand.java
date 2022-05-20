package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.controller.packet.guild.guild.GuildRegionUpdateSizePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import tk.pratanumandal.expr4j.ExpressionEvaluator;

public final class GuildEnlargeCommand {

  private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();
  private final GuildsPlugin plugin;

  public GuildEnlargeCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild enlarge", "g enlarge", "guild powieksz",
      "g powieksz"}, description = "Powiększa teren gildii.")
  public void handleGuildEnlarge(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeYouDontHaveAnyGuild));
    }

    final GuildMember member = guild.getGuildMember(user);
    if (member == null || !member.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeYouCannotEnlarge));
    }

    final int size = guild.getGuildRegion().getSize();
    if (size >= this.plugin.getGuildsConfiguration().pluginWrapper.maxGuildSize) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeGuildHasMaximumSize));
    }

    if (this.plugin.getGuildFactory().findGuildNear(guild.getGuildRegion().getCenterLocation())
        .isPresent()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeAnotherGuildIsNearYour));
    }

    final ItemStack enlargeItem = this.plugin.getGuildsConfiguration().pluginWrapper.enlargeItemWrapper.asItemStack();
    final int amountNeeded = (int) EVALUATOR.evaluate(
        this.plugin.getGuildsConfiguration().pluginWrapper.enlargeAmountCalculation.replace(
                "{ENLARGE_ITEM_AMOUNT}", String.valueOf(enlargeItem.getAmount()))
            .replace("{REGION_SIZE}", String.valueOf(size))
            .replace("{GUILD_CURRENT_MEMBERS_SIZE}", String.valueOf(guild.getCurrentMembersSize()))
            .replace("{GUILD_MEMBERS_SIZE}", String.valueOf(guild.getGuildType().getSize())));
    if (!ItemHelper.hasItem(player, enlargeItem, amountNeeded)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeNoRequiredItem.replace(
              "{AMOUNT_NEEDED}", String.valueOf(amountNeeded))));
    }

    final int newSize = size + this.plugin.getGuildsConfiguration().pluginWrapper.enlargeGuildSize;
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeSuccessSender.replace(
            "{SIZE}", String.valueOf(newSize)));
    ItemHelper.removeItem(player, enlargeItem, amountNeeded);
    guild.getGuildRegion().setSize(newSize);
    guild.startScanningPistons();
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildEnlargeSuccessGuild.replace(
            "{SIZE}", String.valueOf(newSize)).replace("{PLAYER_NAME}", player.getName()));
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildRegionUpdateSizePacket(guild.getTag(), newSize),
            "rhc_master_controller", "rhc_guilds");
  }
}
