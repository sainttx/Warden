package io.ll.warden.utils;

import io.ll.warden.utils.jodd.JDateTime;

/**
 * Creator: LordLambda
 * Date: 5/5/15
 * Project: Warden
 * Usage: A Look Vector
 */
public class LookPosition {

  private float pitch;
  private float yaw;
  private JDateTime time;

  public LookPosition(float pitch, float yaw) {
    this.pitch = pitch;
    this.yaw = yaw;
    this.time = new JDateTime(System.currentTimeMillis());
  }

  public float getPitch() {
    return pitch;
  }

  public float getYaw() {
    return yaw;
  }

  public JDateTime getTime() {
    return time;
  }
}
