package io.ll.warden.check.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.events.PlayerTrueMoveEvent;
import io.ll.warden.utils.BlockUtilities;
import io.ll.warden.utils.MovementHelper;
import io.ll.warden.utils.Timer;

/**
 * Creator: LordLambda
 * Date: 4/8/2015
 * Project: Warden
 * Usage: SIR STOP BREAKING GRAVITY.
 *
 * This is a simple fly check. This checks work by checking the closest block to you
 * aka your distance from the ground. We then check how long you've been above the ground for too
 * long.
 */
public class FlyCheck extends Check implements Listener {

  private ConcurrentHashMap<UUID, Timer> map;
  private BlockUtilities block;
  private MovementHelper move;

  public FlyCheck() {
    super("FlyCheck");
    map = new ConcurrentHashMap<UUID, Timer>();
  }

  @Override
  public void registerListeners(Warden w, PluginManager pm) {
    pm.registerEvents(this, w);
  }

  @Override
  public float getRaiseLevel() {
    return 15f;
  }

  @EventHandler
  public void onMove(PlayerTrueMoveEvent event) {
    UUID u = event.getPlayer().getUniqueId();
    if(block == null) {
      block = BlockUtilities.get();
    }
    if(move == null) {
      move = MovementHelper.get();
    }
    if(map.containsKey(u)) {
      Timer t = map.get(u);
      if(!(move.getPlayerNLocation(u).getBlockY() -
          block.getClosestGroundBlockToPlayer(event.getPlayer()).getBlockY() > 0)) {
        map.remove(u);
      }
      if(t.hasReach(0.5f)) {
        Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
            u, getRaiseLevel(), getName()
        ));
      }
    }else {
      if(move.getPlayerNLocation(u).getBlockY() -
          block.getClosestGroundBlockToPlayer(event.getPlayer()).getBlockY() > 0) {
        map.put(u, new Timer());
      }
    }
  }
}
