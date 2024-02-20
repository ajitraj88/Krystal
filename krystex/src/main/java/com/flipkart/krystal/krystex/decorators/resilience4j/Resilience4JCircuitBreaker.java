package com.flipkart.krystal.krystex.decorators.resilience4j;

import static com.flipkart.krystal.krystex.decorators.resilience4j.R4JUtils.decorateAsyncExecute;
import static com.flipkart.krystal.krystex.decorators.resilience4j.R4JUtils.extractResponseMap;

import com.flipkart.krystal.config.ConfigProvider;
import com.flipkart.krystal.krystex.MainLogic;
import com.flipkart.krystal.krystex.MainLogicDefinition;
import com.flipkart.krystal.krystex.decoration.MainLogicDecorator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.internal.CircuitBreakerStateMachine;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Resilience4JCircuitBreaker implements MainLogicDecorator {

  public static final String DECORATOR_TYPE = Resilience4JCircuitBreaker.class.getName();

  private final String instanceId;

  private @Nullable CircuitBreaker circuitBreaker;

  /**
   * @param instanceId The tag because of which this logic decorator was applied.
   */
  public Resilience4JCircuitBreaker(String instanceId) {
    this.instanceId = instanceId;
  }

  @Override // this input param logicToDecorate is a wrapped logic of main logic and other
  // decorators
  public MainLogic<Object> decorateLogic(
      MainLogic<Object> logicToDecorate, MainLogicDefinition<Object> originalLogicDefinition) {
    CircuitBreaker circuitBreaker = this.circuitBreaker;
    if (circuitBreaker != null) {
      return inputsList -> // this is the lambda.
      extractResponseMap(
              inputsList,
              decorateAsyncExecute(
                      logicToDecorate, inputsList) // this all is Resilience 4J utility.
                  .withCircuitBreaker(circuitBreaker)
                  .get());
    } else {
      return logicToDecorate;
    }
  }

  @Override
  public void onConfigUpdate(ConfigProvider configProvider) {
    updateCircuitBreaker(configProvider);
  }

  @Override
  public String getId() {
    return instanceId;
  }

  private void init(ConfigProvider configProvider) {
    this.circuitBreaker =
        getCircuitBreakerConfig(configProvider)
            .map(config -> new CircuitBreakerStateMachine(instanceId + ".circuit_breaker", config))
            .orElse(null);
  }

  private Optional<CircuitBreakerConfig> getCircuitBreakerConfig(ConfigProvider configProvider) {
    boolean circuitBreakerDisabled =
        !configProvider.<Boolean>getConfig(instanceId + ".circuit_breaker.enabled").orElse(true);
    if (circuitBreakerDisabled) {
      return Optional.empty();
    }
    return Optional.of(CircuitBreakerConfig.ofDefaults());
  }

  private void updateCircuitBreaker(ConfigProvider configProvider) {
    CircuitBreaker circuitBreaker = this.circuitBreaker;
    Optional<CircuitBreakerConfig> newConfig = getCircuitBreakerConfig(configProvider);
    if (!Optional.ofNullable(circuitBreaker)
        .map(CircuitBreaker::getCircuitBreakerConfig)
        .equals(newConfig)) {
      init(configProvider);
    }
  }
}
