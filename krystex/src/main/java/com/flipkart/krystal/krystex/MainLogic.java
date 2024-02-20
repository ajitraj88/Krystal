package com.flipkart.krystal.krystex;

import com.flipkart.krystal.data.Inputs;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public non-sealed interface MainLogic<T> extends Logic {
  // MainLogic takes in ceratin set of inputs and returns CompletableFuture
  ImmutableMap<Inputs, CompletableFuture<@Nullable T>> execute(ImmutableList<Inputs> inputs);
}
