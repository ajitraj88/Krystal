package com.flipkart.krystal.vajramexecutor.krystex;

import com.flipkart.krystal.krystex.KrystalExecutor;
import com.flipkart.krystal.krystex.kryon.KryonExecutionConfig;
import com.flipkart.krystal.krystex.kryon.KryonExecutor;
import com.flipkart.krystal.krystex.kryon.KryonExecutorConfig;
import com.flipkart.krystal.utils.MultiLeasePool;
import com.flipkart.krystal.vajram.ApplicationRequestContext;
import com.flipkart.krystal.vajram.VajramID;
import com.flipkart.krystal.vajram.VajramRequest;
import com.flipkart.krystal.vajram.exec.VajramExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;
// this is a wrapper around VajramExecutor. For every request this is created

public class KrystexVajramExecutor<C extends ApplicationRequestContext>
    implements VajramExecutor<C> {
  // Vajram is the thing which developers write while Kryon is the runtime manifestation of it. So
  // if in your DAG there are 10 vajrams, 10 Kryons are created for it and that is what is executed.
  private final VajramKryonGraph
      vajramKryonGraph; // this is created at the start of the application lifesycle. This is the
  // specific implementation of VajramExecutableGraph. This loads all the
  // Vajrams in memory and creates a DAG.
  private final C applicationRequestContext;
  private final KrystalExecutor krystalExecutor; // this is inside Krystal library.

  public KrystexVajramExecutor(
      VajramKryonGraph vajramKryonGraph,
      C applicationRequestContext,
      MultiLeasePool<? extends ExecutorService> executorServicePool,
      KryonExecutorConfig config) {
    this.vajramKryonGraph = vajramKryonGraph;
    this.applicationRequestContext = applicationRequestContext;
    this.krystalExecutor =
        new KryonExecutor(
            vajramKryonGraph.getKryonDefinitionRegistry(),
            executorServicePool,
            config,
            applicationRequestContext.requestId());
  }

  @Override
  public <T> CompletableFuture<@Nullable T> execute(
      VajramID vajramId, Function<C, VajramRequest> vajramRequestBuilder) {
    return execute(
        vajramId,
        vajramRequestBuilder,
        KryonExecutionConfig.builder().executionId("defaultExecution").build());
  }

  public <T> CompletableFuture<@Nullable T> execute(
      VajramID vajramId,
      Function<C, VajramRequest> vajramRequestBuilder,
      KryonExecutionConfig executionConfig) {
    return krystalExecutor.executeKryon( // now this calls krystalExecutor executeKryon fn.
        vajramKryonGraph.getKryonId(vajramId),
        vajramRequestBuilder.apply(applicationRequestContext).toInputValues(),
        executionConfig);
  }

  public KrystalExecutor getKrystalExecutor() {
    return krystalExecutor;
  }

  @Override
  public void flush() {
    krystalExecutor.flush();
  }

  @Override
  public void close() {
    krystalExecutor.close();
  }
}
