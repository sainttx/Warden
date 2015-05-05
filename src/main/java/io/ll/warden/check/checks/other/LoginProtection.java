package io.ll.warden.check.checks.other;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import io.ll.warden.Warden;
import io.ll.warden.events.BanEvent;
import io.ll.warden.utils.Timer;

/**
 * Creator: LordLambda
 * Date: 4/26/2015
 * Project: Warden
 * Usage: A Login Protection Check.
 *
 * Note this actually doesn't extend check. Since it's not targeting a specific player.
 * It's weird I know. I know. However, I put it in checks because it techincally is checking for
 * something.
 */
public class LoginProtection implements Listener {

  private static LoginProtection instance;
  private boolean setup;
  private boolean shouldWhitelist;
  private boolean whitelistUp;
  private long whitelistBuffer;
  private long timeBetweenLogin;
  private long failureBeforeWhitelist;
  private long currentFailures;
  private BukkitRunnable runnable;
  private Timer sinceLastJoin;

  protected LoginProtection() {
    setup = false;
    shouldWhitelist = true;
    whitelistUp = false;
    whitelistBuffer = -1;
    timeBetweenLogin = -1;
    failureBeforeWhitelist = -1;
    currentFailures = 0;
    runnable = new BukkitRunnable() {
      @Override
      public void run() {
        shouldWhitelist = false;
        if(currentFailures != 0) {
        	currentFailures--;
        }
      }
    };
  }

  public void setup(Warden w, PluginManager pm) {
    if(!setup) {
      pm.registerEvents(this, w);
      if(w.getConfig().getString("LoginProtectionBanOrWhitelist").equalsIgnoreCase("BAN")) {
        shouldWhitelist = false;
      }
      whitelistBuffer = w.getConfig().getLong("LoginWhitelistBuffer");
      if(whitelistBuffer == -1) {
        whitelistBuffer = 2;
      }
      timeBetweenLogin = w.getConfig().getLong("LoginTimeBetweenLogin");
      if(timeBetweenLogin == -1) {
        timeBetweenLogin = 3;
      }
      failureBeforeWhitelist = w.getConfig().getLong("LoginFailureWhitelist");
      if(failureBeforeWhitelist == -1) {
        failureBeforeWhitelist = 5;
      }
      setup = true;
    }
  }

  public static LoginProtection get() {
    if(instance == null) {
      synchronized (LoginProtection.class) {
        if(instance == null) {
          instance = new LoginProtection();
        }
      }
    }
    return instance;
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onLogout(PlayerQuitEvent event) {
    if(!whitelistUp) {
      if(sinceLastJoin == null) {
        sinceLastJoin = new Timer();
        return;
      }
      runnable.cancel();
      if(sinceLastJoin.hasReachMS(timeBetweenLogin * 1000)) {
        sinceLastJoin.reset();
        if(currentFailures != 0) {
        	currentFailures--;
        }
        return;
      }
      sinceLastJoin.reset();
      currentFailures++;
      if(currentFailures >= failureBeforeWhitelist) {
        whitelistUp = true;
        if(!shouldWhitelist) {
          event.getPlayer().setBanned(true);
          Bukkit.getPluginManager().callEvent(new BanEvent(event.getPlayer().getUniqueId(),
                                                           0xDEADBEEF));
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onLogin(PlayerLoginEvent event) {
    if(!whitelistUp) {
      if(sinceLastJoin == null) {
        sinceLastJoin = new Timer();
        return;
      }
      runnable.cancel();
      if(sinceLastJoin.hasReachMS(timeBetweenLogin * 1000)) {
        sinceLastJoin.reset();
        if(currentFailures != 0) {
        	currentFailures--;
        }
        return;
      }
      sinceLastJoin.reset();
      //Logging in too fast!
      currentFailures++;
      if(currentFailures >= failureBeforeWhitelist) {
        whitelistUp = true;
        if(shouldWhitelist) {
          event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Please try connecting later a spam"
                                                                 + "bot attack is going on right"
                                                                 + "now.");
         runnable.runTaskLater(Warden.get(), 20*60*whitelistBuffer);
          // ^ To explain the time on that. 20 ticks ~== 1 second.
          // So therefore 20 ticks * 60 = 1 minute.
          // 1 minute * total minutes == when to run.
        }else {
          event.getPlayer().setBanned(true);
          Bukkit.getPluginManager().callEvent(new BanEvent(event.getPlayer().getUniqueId(),
                                                           0xDEADBEEF));
        }
      }
    }else {
      if(!shouldWhitelist) {
        if(sinceLastJoin == null) {
          sinceLastJoin = new Timer();
          return;
        }
        if(!sinceLastJoin.hasReachMS(timeBetweenLogin * 1000)) {
          event.getPlayer().setBanned(true);
          Bukkit.getPluginManager().callEvent(new BanEvent(event.getPlayer().getUniqueId(),
                                                           0xDEADBEEF));
        }
      }else {
        event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Please try connecting later a spam"
                                                               + "bot attack is going on right"
                                                               + "now.");
      }
    }
  }
}
