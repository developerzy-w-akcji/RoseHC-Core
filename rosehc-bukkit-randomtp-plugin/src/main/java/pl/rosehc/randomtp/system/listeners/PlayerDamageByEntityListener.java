package pl.rosehc.randomtp.system.listeners;

import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.rosehc.randomtp.system.SystemRandomTPPlugin;

public final class PlayerDamageByEntityListener implements Listener {

  private final SystemRandomTPPlugin plugin;

  public PlayerDamageByEntityListener(final SystemRandomTPPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(final EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player) {
      final Player player = (Player) event.getEntity();
      this.findAttacker(event).filter(
              attacker -> !this.plugin.getArenaFactory().findArenaByPlayer(attacker).isPresent()
                  || !this.plugin.getArenaFactory().findArenaByPlayer(player).isPresent())
          .ifPresent(ignored -> event.setCancelled(true));
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
