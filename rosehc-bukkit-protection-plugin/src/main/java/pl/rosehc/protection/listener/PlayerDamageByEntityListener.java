package pl.rosehc.protection.listener;

import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.rosehc.adapter.helper.ChatHelper;
import pl.rosehc.adapter.helper.TimeHelper;
import pl.rosehc.protection.ProtectionPlugin;

public final class PlayerDamageByEntityListener implements Listener {

  private final ProtectionPlugin plugin;

  public PlayerDamageByEntityListener(final ProtectionPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onDamage(final EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player) {
      final Player victim = (Player) event.getEntity();
      this.plugin.getProtectionUserFactory().findUser(victim).ifPresent(
          victimUser -> this.findAttacker(event).ifPresent(
              attacker -> this.plugin.getProtectionUserFactory().findUser(attacker)
                  .ifPresent(attackerUser -> {
                    if (!victimUser.hasExpired() || !attackerUser.hasExpired()) {
                      event.setCancelled(true);
                      ChatHelper.sendMessage(attacker, !attackerUser.hasExpired()
                          ? this.plugin.getProtectionConfiguration().youAreProtected.replace(
                          "{TIME}", TimeHelper.timeToString(
                              attackerUser.getExpiryTime() - System.currentTimeMillis()))
                          : this.plugin.getProtectionConfiguration().targetIsProtected.replace(
                              "{PLAYER_NAME}", victim.getName()).replace("{TIME}",
                              TimeHelper.timeToString(
                                  victimUser.getExpiryTime() - System.currentTimeMillis())));
                    }
                  })));
    }
  }

  private Optional<Player> findAttacker(EntityDamageByEntityEvent event) {
    final Entity attacker = event.getDamager();
    if (attacker instanceof Projectile) {
      final ProjectileSource shooter = ((Projectile) attacker).getShooter();
      return Optional.of(shooter).filter(Player.class::isInstance).map(Player.class::cast);
    }

    return Optional.of(attacker).filter(Player.class::isInstance).map(Player.class::cast);
  }
}
