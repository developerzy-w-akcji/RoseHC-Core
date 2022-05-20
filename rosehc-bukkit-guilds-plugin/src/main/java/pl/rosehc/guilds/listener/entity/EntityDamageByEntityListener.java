package pl.rosehc.guilds.listener.entity;

import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.rosehc.controller.packet.guild.user.GuildUserCacheFighterPacket;
import pl.rosehc.guilds.GuildsPlugin;
import pl.rosehc.guilds.guild.Guild;

public final class EntityDamageByEntityListener implements Listener {

  private final GuildsPlugin plugin;

  public EntityDamageByEntityListener(final GuildsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(final EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player) {
      final Player victim = (Player) event.getEntity();
      this.findAttacker(event).filter(attacker -> !attacker.equals(victim)).flatMap(
              attacker -> this.plugin.getGuildUserFactory().findUserByUniqueId(attacker.getUniqueId()))
          .ifPresent(attackerUser -> this.plugin.getGuildUserFactory()
              .findUserByUniqueId(victim.getUniqueId()).ifPresent(victimUser -> {
                final Guild attackerUserGuild = attackerUser.getGuild(), victimUserGuild = victimUser.getGuild();
                if (Objects.nonNull(attackerUserGuild) && Objects.nonNull(victimUserGuild)) {
                  if (attackerUserGuild.equals(victimUserGuild)) {
                    if (!attackerUserGuild.isPvpGuild()) {
                      event.setCancelled(true);
                      return;
                    }

                    final int damagePercentage = attackerUserGuild.getGuildType()
                        .getDamagePercentage();
                    event.setDamage(
                        damagePercentage >= 1 ? event.getDamage() - (event.getDamage() * (
                            damagePercentage / 100D)) : 0D);
                    return;
                  }

                  if (victimUserGuild.getAlliedGuild() != null && victimUserGuild.getAlliedGuild()
                      .equals(attackerUserGuild) && !(attackerUserGuild.isPvpAlly()
                      && victimUserGuild.isPvpAlly())) {
                    event.setCancelled(true);
                    return;
                  }
                }

                final long fightTime = System.currentTimeMillis()
                    + this.plugin.getGuildsConfiguration().pluginWrapper.parsedVictimKillerConsiderationTimeoutTime;
                victimUser.cacheFighter(attackerUser.getUniqueId(), fightTime);
                this.plugin.getRedisAdapter().sendPacket(
                    new GuildUserCacheFighterPacket(victimUser.getUniqueId(),
                        attackerUser.getUniqueId(), fightTime), "rhc_master_controller",
                    "rhc_guilds");
              }));
    }
  }

  private Optional<Player> findAttacker(final EntityDamageByEntityEvent event) {
    final Entity attacker = event.getDamager();
    if (attacker instanceof Projectile) {
      final ProjectileSource shooter = ((Projectile) attacker).getShooter();
      return Optional.of(shooter).filter(Player.class::isInstance).map(Player.class::cast);
    }

    return Optional.of(attacker).filter(Player.class::isInstance).map(Player.class::cast);
  }
}
