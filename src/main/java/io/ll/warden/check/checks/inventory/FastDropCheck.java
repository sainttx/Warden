package io.ll.warden.check.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.PluginManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.utils.Timer;

/**
 * Creator: LordLambda
 * Date: 4/8/2015
 * Project: Warden
 * Usage: Sir I'm going to have to ask you to
 * leave the supermarket your causing a ruckus.
 *
 * This is a fast drop check. Basically checks to make sure you don't drop to fast.
 */
public class FastDropCheck extends Check implements Listener {

  private ConcurrentHashMap<UUID, Timer> map;
  private long magicValue;

  public FastDropCheck() {
    super("FastDropCheck");
    map = new ConcurrentHashMap<UUID, Timer>();
    magicValue = 100; //Default magic value just incase
  }

  @Override
  public void registerListeners(Warden w, PluginManager pm) {
    pm.registerEvents(this, w);
    magicValue = w.getConfig().getLong("QUICKDROP");
    if (magicValue == -1) {
      magicValue = 100;
    }
  }

  @Override
  public boolean ignoreOnCreative() {
    return false;
  }

  @Override
  public float getRaiseLevel() {
    return 10f;
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    UUID u = event.getPlayer().getUniqueId();
    if (shouldCheckPlayer(u)) {
      if (!map.containsKey(u)) {
        map.put(u, new Timer());
      } else {
        Timer t = map.get(u);
        if (t.hasReachMS(magicValue)) {
          Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
              u, getRaiseLevel(), getName()
          ));
          event.setCancelled(true);
        }
        t.reset();
      }
    }
  }
}
