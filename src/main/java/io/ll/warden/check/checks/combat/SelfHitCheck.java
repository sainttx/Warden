package io.ll.warden.check.checks.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginManager;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;

/**
 * Author: LordLambda
 * Date: 3/23/2015
 * Project: Warden
 * Usage: A simple Self hit check.
 *
 * This is a really easy check. Check if a player has hit themself, by check if the entity that
 * damaged is the same entity that did the damaging. It's really self explanatory. Sometimes
 * this is used to allow people to fly in NCP, and such.
 */
public class SelfHitCheck extends Check implements Listener {

  public SelfHitCheck() {
    super("SelfHitCheck");
  }

  @Override
  public void registerListeners(Warden w, PluginManager pm) {
    pm.registerEvents(this, w);
  }

  @Override
  public boolean ignoreOnCreative() {
    return false;
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player &&
        event.getEntity() instanceof Player) {
      Player attacker = (Player) event.getDamager();
      Player attacked = (Player) event.getEntity();
      if(shouldCheckPlayer(attacker.getUniqueId())) {
        if(attacker.equals(attacked)) {
          Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
              attacker.getUniqueId(), getRaiseLevel(), getName()
          ));
        }
      }
    }
  }

  @Override
  public float getRaiseLevel() {
    return 10f;
  }
}
