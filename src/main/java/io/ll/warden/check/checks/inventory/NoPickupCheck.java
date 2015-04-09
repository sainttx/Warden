package io.ll.warden.check.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.events.PlayerTrueMoveEvent;
import io.ll.warden.utils.MathHelper;
import io.ll.warden.utils.MovementHelper;

/**
 * Creator: LordLambda
 * Date: 4/8/2015
 * Project: Warden
 * Usage: Sir you payed for these items, you must
 * accept them.
 *
 * This is implemented on a few clients, and it's not really like useful. It's just to stop people
 * who I guess are using this. Which I mean. No real advantage, but it helps us catch people who are
 * using a client so we can catch them. By the way the default pickup distance is 4. I run this in a
 * seperate thread because you have to iterate through a list of items to figure out whats on the
 * ground. It sucks.
 */
public class NoPickupCheck extends Check implements Listener {

  private Thread checkingThread;
  private MovementHelper mh;
  private ConcurrentHashMap<UUID, Location> locations;

  public NoPickupCheck() {
    super("NoPickupCheck");
    locations = new ConcurrentHashMap<UUID, Location>();
    checkingThread = new Thread() {
      @Override
      public void run() {
        while (true) {
          if (locations.isEmpty()) {
            continue;
          } else {
            UUID key = locations.keys().nextElement();
            Location value = locations.get(key);
            locations.remove(key);
            List<Entity> entities = value.getWorld().getEntities();
            for (Entity cItem : entities) {
              if (cItem instanceof Item) {
                Item item = (Item) cItem;
                if (MathHelper.getHorizontalDistance(value, item.getLocation()) < 4) {
                  Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
                      key, getRaiseLevel(), getName()
                  ));
                }
              }
            }
          }
        }
      }
    };
  }

  @Override
  public void registerListeners(Warden w, PluginManager pm) {
    pm.registerEvents(this, w);
    checkingThread.start();
  }

  @Override
  public float getRaiseLevel() {
    return 4f;
  }

  @EventHandler
  public void onMove(PlayerTrueMoveEvent event) {
    if (mh == null) {
      mh = MovementHelper.get();
    }
    UUID u = event.getPlayer().getUniqueId();
    if (shouldCheckPlayer(u)) {
      locations.put(u, mh.getPlayerNLocation(u));
    }
  }
}
