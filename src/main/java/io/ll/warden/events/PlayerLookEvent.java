package io.ll.warden.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.ll.warden.utils.LookPosition;
import io.ll.warden.utils.jodd.JDateTime;

/**
 * Creator: LordLambda
 * Date: 5/5/15
 * Project: Warden
 * Usage: When A player looks
 */
public class PlayerLookEvent extends Event {

  public static final HandlerList handlers = new HandlerList();

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  private Player player;
  private LookPosition lp;

  public PlayerLookEvent(Player player, LookPosition lp) {
    this.player = player;
    this.lp = lp;
  }

  public Player getPlayer() {
    return player;
  }

  public LookPosition getLookPosition() {
    return lp;
  }
}
