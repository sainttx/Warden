package io.ll.warden.check.checks.other;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import io.ll.warden.Warden;
import io.ll.warden.check.Check;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.events.PlayerLookEvent;
import io.ll.warden.utils.LookPosition;
import io.ll.warden.utils.MovementHelper;

/**
 * Creator: LordLambda
 * Date: 5/5/15
 * Project: Warden
 * Usage: The Mouse Possible Check
 */
public class MousePossibleCheck extends Check implements Listener {

  public MousePossibleCheck() {
    super("MOUSEPOSSIBLE");
  }

  @Override
  public void registerListeners(Warden w, PluginManager pm) {
    pm.registerEvents(this, w);
  }

  @Override
  public boolean ignoreOnCreative() {
    return false;
  }

  @Override
  public float getRaiseLevel() {
    return 5f;
  }

  @EventHandler
  public void onLook(PlayerLookEvent event) {
    LookPosition first = event.getLookPosition();
    LookPosition second = MovementHelper.get()
        .getPlayerNMinusOneLookPosition(event.getPlayer().getUniqueId());
    if(second == null) {
      return;
    }
    if(first.getPitch() != second.getPitch() || first.getYaw() != second.getYaw()) {
      ////////////////////////////////////////////////////////////
      //
      //
      // This check is based off the fact that the way mice work.
      //  that a mouse cannot move in a direction without moving
      //  in the other direction. So this will need some more
      //  testing. Because if it's a little bit off. I haven't
      //  noticed any false positives yet, but more testing is
      //  needed.
      //
      //
      ////////////////////////////////////////////////////////////
      if(first.getPitch() == second.getPitch() && first.getYaw() != second.getYaw()) {
        Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
            event.getPlayer().getUniqueId(), getRaiseLevel(), getName()
        ));
      }else if(first.getPitch() != second.getPitch() && first.getYaw() == second.getYaw()) {
        Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
            event.getPlayer().getUniqueId(), getRaiseLevel(), getName()
        ));
      }else {
        long deltaTime = second.getTime().getTimeInMillis() - first.getTime().getTimeInMillis();
        long deltaYaw = (long) Math.abs(Math.abs(second.getYaw()) - Math.abs(first.getYaw()));
        long deltaPitch = (long) Math.abs(Math.abs(second.getPitch()) - Math.abs(first.getPitch()));

        long yawPerSecond = deltaYaw / deltaTime;
        long pitchPerSecond = deltaPitch / deltaTime;

        if(yawPerSecond > 100 || pitchPerSecond > 100) { //TODO: More testing here.
          Bukkit.getPluginManager().callEvent(new CheckFailedEvent(
              event.getPlayer().getUniqueId(), getRaiseLevel(), getName()
          ));
        }
      }
    }
  }
}
