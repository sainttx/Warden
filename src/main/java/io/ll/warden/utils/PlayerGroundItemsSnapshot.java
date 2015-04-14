package io.ll.warden.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.ll.warden.utils.jodd.JDateTime;

/**
 * Creator: LordLambda
 * Date: 4/13/2015
 * Project: Warden
 * Usage: A snapshot of all the items in the world
 */
public class PlayerGroundItemsSnapshot {

  private List<Item> onGroundItems;
  private JDateTime timeTaken;
  private UUID player;
  private boolean inventoryFull;
  private Location playerLocation;

  public PlayerGroundItemsSnapshot(UUID u, World w) {
    player = u;
    inventoryFull = Bukkit.getPlayer(u).getInventory().firstEmpty() == -1;
    playerLocation = Bukkit.getPlayer(u).getLocation();
    List<Entity> entities = w.getEntities();
    timeTaken = new JDateTime(System.currentTimeMillis());
    onGroundItems = new ArrayList<Item>();
    for(Entity e : entities) {
      if(e instanceof Item) {
        onGroundItems.add((Item) e);
      }
    }
  }

  public JDateTime getTimeTaken() {
    return timeTaken;
  }

  public List<Item> getItemSnapshot() {
    return onGroundItems;
  }

  public boolean wasInventoryFull() {
    return inventoryFull;
  }

  public Location getPlayerLocation() {
    return playerLocation;
  }

  public UUID getPlayer() {
    return player;
  }
}
