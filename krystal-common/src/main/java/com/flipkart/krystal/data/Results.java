package com.flipkart.krystal.data;

import com.google.common.collect.ImmutableMap;

public record Results<T>(ImmutableMap<Inputs, ValueOrError<T>> values) implements InputValue<T> {

  private static final Results<?> EMPTY = new Results<>(ImmutableMap.of());

  public static <T> Results<T> empty() {
    //noinspection unchecked
    return (Results<T>) EMPTY;
  }
}
