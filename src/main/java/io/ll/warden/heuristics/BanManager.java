package io.ll.warden.heuristics;

import io.ll.warden.storage.Storage;
import io.ll.warden.utils.proxy.Warden;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.ll.warden.commands.AuthAction;
import io.ll.warden.configuration.BanConfig;
import io.ll.warden.events.BanEvent;
import io.ll.warden.events.CheckFailedEvent;
import io.ll.warden.utils.UUIDFetcher;
import io.ll.warden.utils.ViolationLevelWithPoints;

/**
 * Creator: LordLambda
 * Date: 3/25/2015
 * Project: Warden
 * Usage: A simple ban manager.
 *
 * This also handles things like when people fail checks what level of "Hacker" they are at.
 */
public class BanManager implements Listener, CommandExecutor, AuthAction.AuthCallback {

  private Storage storage;
  private ConcurrentHashMap<UUID, ViolationLevelWithPoints> hackerMap;
  private FileConfiguration config;
  private ConcurrentHashMap<UUID, String[]> waitingForVerification;
  private ConcurrentHashMap<UUID, BanConfig> banConfigs;
  private List<UUID> bannedByWarden;
  private Thread constantBanThread;
  private Thread constanstCheckBanThread;
  private int[] pointsNeededPerLevel;
  private ViolationLevel banAt;

  public BanManager() {
    hackerMap = new ConcurrentHashMap<UUID, ViolationLevelWithPoints>();
    banConfigs = new ConcurrentHashMap<UUID, BanConfig>();
    waitingForVerification = new ConcurrentHashMap<UUID, String[]>();
    bannedByWarden = new ArrayList<UUID>();
    constantBanThread = new Thread() {
      @Override
      public void run() {
        for (UUID u : bannedByWarden) {
          Player p = Bukkit.getServer().getPlayer(u);
          if (!p.isBanned()) {
            p.setBanned(true);
          }
        }
      }
    };
    constanstCheckBanThread = new Thread() {
      @Override
      public void run() {
        for (UUID u : hackerMap.keySet()) {
          ViolationLevelWithPoints vlwp = hackerMap.get(u);
          int points = vlwp.getPoints();
          ViolationLevel vl = pointsToLevel(points);
          if (vl != vlwp.getLevel()) {
            storage.doQuery(String.format("UPDATE WardenBans SET HLEVEL=%d WHERE"
                    + "UUID='%s'", vl.ordinal(), u));
            hackerMap.put(u, new ViolationLevelWithPoints(vl, points));
          }
          //Update if neccesarry
          vlwp = hackerMap.get(u);
          if (vlwp.getLevel().ordinal() >= banAt.ordinal()) {
            Bukkit.getPlayer(u).setBanned(true);
            storage.doQuery(String.format("UPDATE WardenBans SET ISBANNEDBYWARDEN=1 WHERE"
                    + "UUID='%s'", u));
            bannedByWarden.add(u);
            Bukkit.getPluginManager().callEvent(new BanEvent(
              u, points
            ));
          }
        }
      }
    };
  }

  public void setup(Storage storage) {
    pointsNeededPerLevel = new int[]{
        0, config.getInt("SMALLTIME"), config.getInt("MID"), config.getInt("HIGH"),
        config.getInt("HIGHEST")
    };
    banAt = ViolationLevel.valueOf(config.getString("BANAT"));
    if (banAt == null) {
      banAt = ViolationLevel.HIGH;
    }
    this.storage = storage;
    this.storage.doQuery("CREATE TABLE IF NOT EXISTS WardenBans"
            + "(UUID varchar(36) NOT NULL,"
            + "HLEVEL tinyint(1) NOT NULL,"
            + "HPOINTS int(4) NOT NULL,"
            + "ISBANEEDBYWARDEN tinyint(1) NOT NULL,"
            + "PRIMARY KEY (UUID)"
            + ")");
    List<Map<String, Object>> queryResult = this.storage.doQuery("SELECT UUID From WardenBans", "UUID", "HPOINTS");

    for (Map<String, Object> setResult : queryResult) {
      UUID uuid = UUID.fromString((String) setResult.get("UUID"));
      int points = (Integer) setResult.get("HPOINTS");
    }

    File banDir = new File(Warden.getDataFolder().get(), "Warden/Bans");
    if(!banDir.exists()) {
      banDir.mkdir();
    }else {
      for(File f : banDir.listFiles()) {
        try {
          BanConfig bc = new BanConfig(f.getName(), true);
          UUID uuid = UUID.fromString(bc.getFullName().substring(0, bc.getFullName()
              .lastIndexOf('.')));
          banConfigs.put(uuid, bc);
        }catch(Exception e1) {
          Warden.logSevere(String.format("Failed to load banconfig: [ %s ]", f.getAbsolutePath()));
          e1.printStackTrace();
        }
      }
    }
    Bukkit.getPluginManager().registerEvents(this, Warden.getPluginContainer().get());
    constanstCheckBanThread.start();
    constantBanThread.start();
  }

  @EventHandler
  public void onCheckFail(final CheckFailedEvent event) {
    //Execute SQL
    new Thread() {
      @Override
      public void run() {
        UUID u = event.getPlayer();
        ViolationLevelWithPoints vlwp = hackerMap.get(u);
        hackerMap.put(u, new ViolationLevelWithPoints(vlwp.getLevel(), vlwp.getPoints() +
                                                                       (int) event.getDamage()));
        vlwp = hackerMap.get(u);
        storage.doQuery(String.format("UPDATE WardenBans SET HPOINTS=%d WHERE"
                + "UUID='%s'", vlwp.getPoints(), u));
      }
    }.run();
    BanConfig bc = banConfigs.get(event.getPlayer());
    bc.addCheckFailed(event.getCheckName(), event.getPlayer(), event.getDamage());
  }

  @EventHandler
  public void onBan(final BanEvent event) {
    //SQL was already executed. Now we need to save to file.
    BanConfig bc = banConfigs.get(event.getUUID());
    bc.addBan(event.getPoints());
  }

  @EventHandler
  public void playerLoginEvent(PlayerLoginEvent event) {
    if (!hackerMap.containsKey(event.getPlayer().getUniqueId())) {
      //First time login add to table
      storage.doQuery(String.format("INSERT INTO WardenBans ("
              + "UUID, HLEVEL, HPOINTS, ISBANNEDBYWARDEN)"
              + "VALUES ('%s', 0, 0, 0)", event.getPlayer().getUniqueId()));
      hackerMap.put(event.getPlayer().getUniqueId(), new ViolationLevelWithPoints(
          ViolationLevel.NONE, 0));
    }
  }

  private ViolationLevel pointsToLevel(int points) {
    if (points < pointsNeededPerLevel[1]) {
      return ViolationLevel.NONE;
    } else if (points >= pointsNeededPerLevel[1] && points < pointsNeededPerLevel[2]) {
      return ViolationLevel.SMALLTIME;
    } else if (points >= pointsNeededPerLevel[2] && points < pointsNeededPerLevel[3]) {
      return ViolationLevel.MID;
    } else if (points >= pointsNeededPerLevel[3] && points < pointsNeededPerLevel[4]) {
      return ViolationLevel.HIGH;
    } else if (points >= pointsNeededPerLevel[5]) {
      return ViolationLevel.BRUDIN;
    }
    return null;
  }

  public enum ViolationLevel {
    NONE,
    SMALLTIME,
    MID,
    HIGH,
    BRUDIN; //Also known as HIGHEST

    public static ViolationLevel getByOrdinal(int ord) {
      for (ViolationLevel vl : values()) {
        if (vl.ordinal() == ord) {
          return vl;
        }
      }
      return null;
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
    if(name.equalsIgnoreCase("queryBan") || name.equalsIgnoreCase("qB")) {
      if(!(sender instanceof Player)) {
        Warden.logWarning("This commands can only be called by a player!");
        return true;
      }

      Player theSender = (Player) sender;
      if(args.length != 1) {
        theSender.sendMessage(String.format("[Warden] Incorrect Args!"));
        return false;
      }
      String playerName = args[0];
      UUID u;
      try {
        u = UUIDFetcher.getUUIDOf(playerName);
      }catch(Exception e1) {
        theSender.sendMessage("[Warden] Failed to grab UUID of specified name!");
        e1.printStackTrace();
        return false;
      }
      BanConfig bc = banConfigs.get(u);
      //TODO: Split into length so it displays in chat, and send message to player
    }else if(name.equalsIgnoreCase("queryChecks") || name.equalsIgnoreCase("qC")) {
      if(!(sender instanceof Player)) {
        Warden.logWarning("This commands can only be called by a player!");
        return false;
      }
      Player theSender = (Player) sender;
      if(args.length != 1) {
        theSender.sendMessage(String.format("[Warden] Incorrect Args!"));
        return false;
      }
      String playerName = args[0];
      UUID u;
      try {
        u = UUIDFetcher.getUUIDOf(playerName);
      }catch(Exception e1) {
        theSender.sendMessage("[Warden] Failed to grab UUID of specified name!");
        e1.printStackTrace();
        return false;
      }
      BanConfig bc = banConfigs.get(u);
      //TODO: Split into length so it displays in chat, and send message to player
    }
    return true;
  }

  @Override
  public void onCallback(UUID u, AuthAction.AuthLevel level) {

  }

  @Override
  public String getAuthBackName() {
    return "BanManager";
  }
}
