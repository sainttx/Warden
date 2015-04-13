package io.ll.warden.configuration;

/**
 * Creator: LordLambda
 * Date: 3/28/2015
 * Project: Warden
 * Usage: A convience method for Configuration Parsing
 */
public class Value<T> {

  private T value;

  public Value(T value) {
    this.value = value;
  }

  public T getValue() {
    return this.value;
  }
}
