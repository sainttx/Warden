package io.ll.warden.check.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.events.PlayerTrueMoveEvent;
import io.ll.warden.utils.MathHelper;
import io.ll.warden.utils.MovementHelper;
import io.ll.warden.utils.PlayerGroundItemsSnapshot;

/**
 * Creator: LordLambda
 * Date: 4/8/2015
 * Project: Warden
 * Usage: Sir you payed for these items, you must accept them.
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
  private List<PlayerGroundItemsSnapshot> snapshots;

  public NoPickupCheck() {
    super("NoPickupCheck");
    snapshots = new CopyOnWriteArrayList<PlayerGroundItemsSnapshot>();
    checkingThread = new Thread() {
      @Override
      public void run() {
        while (true) {
          PlayerGroundItemsSnapshot cSnapshot = snapshots.get(0);
          if (cSnapshot != null) {
            if (!cSnapshot.wasInventoryFull()) {
              List<Item> items = cSnapshot.getItemSnapshot();
              for (Item i : items) {
                if (MathHelper.getHorizontalDistance(
                    i.getLocation(), cSnapshot.getPlayerLocation()
                ) < 4) {
                  Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
                      cSnapshot.getPlayer(), getRaiseLevel(), getName()
                  ));
                  break;
                }
              }
            }
            snapshots.remove(cSnapshot);
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
  public boolean ignoreOnCreative() {
    return false;
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
      snapshots.add(new PlayerGroundItemsSnapshot(event.getPlayer().getUniqueId(),
                                                  event.getPlayer().getWorld()));
    }
  }
}
