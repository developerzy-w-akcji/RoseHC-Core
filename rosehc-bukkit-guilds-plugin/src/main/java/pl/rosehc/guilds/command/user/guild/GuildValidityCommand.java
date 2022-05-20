package pl.rosehc.guilds.command.user.guild;

import me.vaperion.blade.annotation.Command;
import me.vaperion.blade.annotation.Sender;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.ItemHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.controller.packet.guild.guild.GuildValidityTimeUpdatePacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;
import pl.rosehc.guilds.guild.GuildMember;
import pl.rosehc.guilds.user.GuildUser;
import pl.rosehc.platform.PlatformPlugin;
import tk.pratanumandal.expr4j.ExpressionEvaluator;

public final class GuildValidityCommand {

  private static final ExpressionEvaluator EVALUATOR = new ExpressionEvaluator();
  private final GuildsPlugin plugin;

  public GuildValidityCommand(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(value = {"guild validity", "g validity", "guild przedluz",
      "g przedluz"}, description = "Przedłuża ważność gildii.")
  public void handleGuildEnlarge(final @Sender Player player) {
    final GuildUser user = this.plugin.getGuildUserFactory()
        .findUserByUniqueId(player.getUniqueId()).orElseThrow(() -> new BladeExitMessage(
            ChatHelper.colored(PlatformPlugin.getInstance()
                .getPlatformConfiguration().messagesWrapper.playerNotFound.replace("{PLAYER_NAME}",
                    player.getName()))));
    final Guild guild = user.getGuild();
    if (guild == null) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildValidityYouDontHaveAnyGuild));
    }

    final GuildMember member = guild.getGuildMember(user);
    if (member == null || !member.canManage()) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildValidityYouCannotExtendTime));
    }

    if (this.plugin.getGuildsConfiguration().pluginWrapper.parsedWhenGuildValidityTime > 0L) {
      final long whenDelta = guild.getValidityTime() - System.currentTimeMillis();
      if (whenDelta
          > this.plugin.getGuildsConfiguration().pluginWrapper.parsedWhenGuildValidityTime) {
        throw new BladeExitMessage(ChatHelper.colored(
            this.plugin.getGuildsConfiguration().messagesWrapper.guildValidityWhenYouCanExtendTime.replace(
                "{TIME}", TimeHelper.timeToString(whenDelta
                    - this.plugin.getGuildsConfiguration().pluginWrapper.parsedWhenGuildValidityTime))));
      }
    }

    if (this.plugin.getGuildsConfiguration().pluginWrapper.parsedMaxGuildValidityTime > 0L &&
        guild.getValidityTime()
            + this.plugin.getGuildsConfiguration().pluginWrapper.parsedMaxGuildValidityTime
            > System.currentTimeMillis()
            + this.plugin.getGuildsConfiguration().pluginWrapper.parsedMaxGuildValidityTime) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildValidityMaxTimeExceed));
    }

    final ItemStack validityItem = this.plugin.getGuildsConfiguration().pluginWrapper.validityItemWrapper.asItemStack();
    final int amountNeeded = (int) EVALUATOR.evaluate(
        this.plugin.getGuildsConfiguration().pluginWrapper.validityAmountCalculation.replace(
                "{VALIDITY_ITEM_AMOUNT}", String.valueOf(validityItem.getAmount()))
            .replace("{REGION_SIZE}", String.valueOf(guild.getGuildRegion().getSize()))
            .replace("{GUILD_CURRENT_MEMBERS_SIZE}", String.valueOf(guild.getCurrentMembersSize()))
            .replace("{GUILD_MEMBERS_SIZE}", String.valueOf(guild.getGuildType().getSize())));
    if (!ItemHelper.hasItem(player, validityItem, amountNeeded)) {
      throw new BladeExitMessage(ChatHelper.colored(
          this.plugin.getGuildsConfiguration().messagesWrapper.guildValidityNoRequiredItem.replace(
              "{AMOUNT_NEEDED}", String.valueOf(amountNeeded))));
    }

    final long validityTime =
        (guild.getValidityTime() == 0L ? System.currentTimeMillis() : guild.getValidityTime())
            + this.plugin.getGuildsConfiguration().pluginWrapper.parsedAddGuildValidityTime;
    ChatHelper.sendMessage(player,
        this.plugin.getGuildsConfiguration().messagesWrapper.guildValiditySuccessSender.replace(
                "{TIME}", TimeHelper.timeToString(validityTime - System.currentTimeMillis()))
            .replace("{DATE}", TimeHelper.dateToString(validityTime)));
    ItemHelper.removeItem(player, validityItem, amountNeeded);
    guild.setValidityTime(validityTime);
    guild.broadcastChatMessage(
        this.plugin.getGuildsConfiguration().messagesWrapper.guildValiditySuccessGuild.replace(
                "{PLAYER_NAME}", player.getName())
            .replace("{TIME}", TimeHelper.timeToString(validityTime - System.currentTimeMillis()))
            .replace("{DATE}", TimeHelper.dateToString(validityTime)));
    this.plugin.getRedisAdapter()
        .sendPacket(new GuildValidityTimeUpdatePacket(guild.getTag(), validityTime),
            "rhc_master_controller", "rhc_guilds");
  }
}
