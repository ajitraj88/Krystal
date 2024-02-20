package com.flipkart.krystal.krystex.kryon;

import com.flipkart.krystal.krystex.MainLogicDefinition;
import com.flipkart.krystal.krystex.commands.KryonCommand;
import com.flipkart.krystal.krystex.decoration.LogicDecorationOrdering;
import com.flipkart.krystal.krystex.decoration.LogicExecutionContext;
import com.flipkart.krystal.krystex.decoration.MainLogicDecorator;
import com.flipkart.krystal.krystex.kryon.KryonDefinition.KryonDefinitionView;
import com.flipkart.krystal.krystex.request.RequestIdGenerator;
import com.flipkart.krystal.krystex.resolution.ResolverDefinition;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;

abstract sealed class AbstractKryon<C extends KryonCommand, R extends KryonResponse>
    implements Kryon<C, R> permits BatchKryon, GranularKryon {

  protected final KryonDefinition kryonDefinition;
  protected final KryonId kryonId;
  protected final KryonExecutor kryonExecutor;
  /** decoratorType -> Decorator */
  protected final Function<LogicExecutionContext, ImmutableMap<String, MainLogicDecorator>>
      requestScopedDecoratorsSupplier;

  protected final LogicDecorationOrdering logicDecorationOrdering;

  protected final ImmutableMap<Optional<String>, ImmutableSet<ResolverDefinition>>
      resolverDefinitionsByInput;
  protected final ImmutableMap<String, ImmutableSet<ResolverDefinition>>
      resolverDefinitionsByDependencies;
  protected final ImmutableSet<String> dependenciesWithNoResolvers;
  protected final RequestIdGenerator requestIdGenerator;
  // this AbstractKryon is called inside batchKryon where request scope decorators are passed as
  // well.
  AbstractKryon(
      KryonDefinition kryonDefinition,
      KryonExecutor kryonExecutor,
      Function<LogicExecutionContext, ImmutableMap<String, MainLogicDecorator>>
          requestScopedDecoratorsSupplier,
      LogicDecorationOrdering logicDecorationOrdering,
      RequestIdGenerator requestIdGenerator) {
    this.kryonDefinition = kryonDefinition;
    this.kryonId = kryonDefinition.kryonId();
    this.kryonExecutor = kryonExecutor;
    this.requestScopedDecoratorsSupplier =
        requestScopedDecoratorsSupplier; // so when the Kryon is created you give the
    // requestScopedDecorators to be applied.
    this.logicDecorationOrdering = logicDecorationOrdering;
    KryonDefinitionView kryonDefinitionView = kryonDefinition.kryonDefinitionView();
    this.resolverDefinitionsByInput = kryonDefinitionView.resolverDefinitionsByInput();
    this.resolverDefinitionsByDependencies =
        kryonDefinitionView.resolverDefinitionsByDependencies();
    this.dependenciesWithNoResolvers = kryonDefinitionView.dependenciesWithNoResolvers();
    this.requestIdGenerator = requestIdGenerator;
  }
  // this gets the decoration order and order given by you is made as the decoration order.
  protected NavigableSet<MainLogicDecorator> getSortedDecorators(DependantChain dependantChain) {
    MainLogicDefinition<Object> mainLogicDefinition = kryonDefinition.getMainLogicDefinition();
    Map<String, MainLogicDecorator> decorators =
        new LinkedHashMap<>(
            mainLogicDefinition.getSessionScopedLogicDecorators(
                kryonDefinition,
                dependantChain)); // mainLogicDefinition contains decorators. Decorators are
    // wrapping the main logic.
    // If the same decoratorType is configured for session and request scope, request scope
    // overrides session scope.
    decorators
        .putAll( // request scope decorators. Ordering of session and request decorators depends on
            // functionality.
            requestScopedDecoratorsSupplier.apply(
                new LogicExecutionContext(
                    kryonId,
                    mainLogicDefinition.logicTags(),
                    dependantChain,
                    kryonDefinition.kryonDefinitionRegistry())));
    TreeSet<MainLogicDecorator> sortedDecorators =
        new TreeSet<>(
            logicDecorationOrdering
                .decorationOrder()); // ordered Set ie the order you want. ie ths is the final set
    // of ordered decorators.
    sortedDecorators.addAll(decorators.values());
    return sortedDecorators;
  }
}
