package io.ll.warden.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Creator: LordLambda
 * Date: 4/22/2015
 * Project: Warden
 * Usage: Precomputes certain values. Right now only used for the speed check, but will be used
 * later on for more things
 */
public class ValuePrecomputer {

  private static ValuePrecomputer instance;
  private ConcurrentHashMap<Integer, Float> precomputedSpeedPotionValues;
  private ConcurrentHashMap<Integer, Float> precomputedSlowPotionValues;

  protected ValuePrecomputer() {
    precomputedSlowPotionValues = new ConcurrentHashMap<Integer, Float>();
    precomputedSpeedPotionValues = new ConcurrentHashMap<Integer, Float>();
  }

  public static ValuePrecomputer get() {
    if(instance == null) {
      synchronized (ValuePrecomputer.class) {
        if(instance == null) {
          instance = new ValuePrecomputer();
        }
      }
    }
    return instance;
  }

  public float getSpeedValue(int amplifier) {
    if(!precomputedSpeedPotionValues.containsKey(amplifier)) {
      float toAdd = 1F;
      toAdd *= 1.0f + (0.2f * (amplifier + 1));
      precomputedSpeedPotionValues.put(amplifier, toAdd);
    }
    return precomputedSpeedPotionValues.get(amplifier);
  }

  public float getSlownessValue(int amplifier) {
    if(!precomputedSlowPotionValues.containsKey(amplifier)) {
      float toAdd = 1F;
      toAdd *= 1.0f - (0.15f * (amplifier + 1));
      precomputedSlowPotionValues.put(amplifier, toAdd);
    }
    return precomputedSlowPotionValues.get(amplifier);
  }
}
