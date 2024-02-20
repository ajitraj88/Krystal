package com.flipkart.krystal.krystex.decorators.observability;

import static com.flipkart.krystal.data.ValueOrError.empty;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.concurrent.CompletableFuture.allOf;

import com.flipkart.krystal.data.Inputs;
import com.flipkart.krystal.data.Results;
import com.flipkart.krystal.data.ValueOrError;
import com.flipkart.krystal.krystex.MainLogic;
import com.flipkart.krystal.krystex.MainLogicDefinition;
import com.flipkart.krystal.krystex.decoration.MainLogicDecorator;
import com.flipkart.krystal.krystex.kryon.KryonId;
import com.flipkart.krystal.krystex.kryon.KryonLogicId;
import com.google.common.collect.ImmutableMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
// WIWWT decorator. This is the initial version of WIWWT decorator and is not optimal and for big
// graph it crashes. Key contains the json inputs,
// For adder it has inputs numberOne and numberTwo which are small but for Zulu which are very big
// and if you run it on DAL, those whole Zulu objects
// were serialising here, so that's why jackson was throwing OOM exception. Use it as a base but we
// want is this should open as a graph(draft).

public class MainLogicExecReporter implements MainLogicDecorator {

  private final KryonExecutionReport kryonExecutionReport;

  public MainLogicExecReporter(KryonExecutionReport kryonExecutionReport) {
    this.kryonExecutionReport = kryonExecutionReport;
  }

  @Override
  public MainLogic<Object> decorateLogic(
      MainLogic<Object> logicToDecorate, MainLogicDefinition<Object> originalLogicDefinition) {
    return inputs -> {
      KryonId kryonId = originalLogicDefinition.kryonLogicId().kryonId();
      KryonLogicId kryonLogicId = originalLogicDefinition.kryonLogicId();
      /*
       Report logic start
      */
      kryonExecutionReport.reportMainLogicStart(kryonId, kryonLogicId, inputs);

      /*
       Execute logic
      */
      ImmutableMap<Inputs, CompletableFuture<@Nullable Object>> results =
          logicToDecorate.execute(inputs);
      /*
       Report logic end
      */
      allOf(results.values().toArray(CompletableFuture[]::new))
          .whenComplete(
              (unused, throwable) -> {
                kryonExecutionReport.reportMainLogicEnd(
                    kryonId,
                    kryonLogicId,
                    new Results<>(
                        results.entrySet().stream()
                            .collect(
                                toImmutableMap(
                                    Entry::getKey,
                                    e ->
                                        e.getValue()
                                            .handle(ValueOrError::valueOrError)
                                            .getNow(empty())))));
              });
      return results;
    };
  }

  @Override
  public String getId() {
    return MainLogicExecReporter.class.getName();
  }
}
