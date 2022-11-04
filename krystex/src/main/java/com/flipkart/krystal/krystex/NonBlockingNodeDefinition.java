package com.flipkart.krystal.krystex;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract non-sealed class NonBlockingNodeDefinition<T> extends NodeDefinition<T> {

  NonBlockingNodeDefinition(String nodeId, Map<String, String> inputs) {
    super(nodeId, inputs);
  }

  @Override
  public final CompletableFuture<ImmutableList<T>> logic(ImmutableMap<String, ?> dependencyValues) {
    return completedFuture(nonBlockingLogic(dependencyValues));
  }

  protected abstract ImmutableList<T> nonBlockingLogic(ImmutableMap<String, ?> dependencyValues);
}
