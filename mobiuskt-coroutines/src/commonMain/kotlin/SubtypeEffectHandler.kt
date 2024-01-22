package kt.mobius.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

public fun <F : Any, E> subtypeEffectHandler(
    executionPolicy: ExecutionPolicy,
    block: SubtypeEffectHandlerBuilder<F, E>.() -> Unit
): FlowTransformer<F, E> =
    SubtypeEffectHandlerBuilder<F, E>(executionPolicy)
        .apply(block)
        .build()

public fun <F : Any, E> subtypeEffectHandler(
    block: SubtypeEffectHandlerBuilder<F, E>.() -> Unit
): FlowTransformer<F, E> =
    SubtypeEffectHandlerBuilder<F, E>(ExecutionPolicy.Concurrent)
        .apply(block)
        .build()

private val NOOP_COLLECTOR: suspend FlowCollector<Nothing>.(effect: Any) -> Unit = {}

public class SubtypeEffectHandlerBuilder<F : Any, E>(
    @PublishedApi
    internal val defaultExecutionPolicy: ExecutionPolicy
) {
    private val effectPerformerMap = hashMapOf<KClass<*>, FlowTransformer<F, E>>()

    /**
     * Provides the Flow of Effects and returns a Flow of Events.
     * This method is unaffected by [ExecutionPolicy]s, allowing you
     * to provide custom behavior directly to the flow.
     *
     * @param effectHandler A lambda that accepts a Flow of effects and returns a flow of Events.
     */
    public inline fun <reified G : F> addTransformer(
        noinline effectHandler: (input: Flow<G>) -> Flow<E>
    ) {
        addFlowTransformer(G::class, effectHandler)
    }

    /**
     * Ignore this type of Effect, preventing errors for unhandled Effects.
     */
    public inline fun <reified G : F> ignore() {
        addHandler(G::class, ExecutionPolicy.Latest)
    }

    /**
     * Receives one Effect and returns exactly one Event.
     *
     * @see addConsumer to return zero Events.
     * @see addValueCollector to emit more than one Event.
     *
     * @param executionPolicy The policy defining coroutines behavior.
     * @param effectHandler A suspending lambda that accepts the Effect and returns an Event.
     */
    public inline fun <reified G : F> addFunction(
        executionPolicy: ExecutionPolicy,
        noinline effectHandler: suspend (effect: G) -> E
    ) {
        addHandler(G::class, executionPolicy) { emit(effectHandler(it)) }
    }

    /**
     * Receives one Effect and returns exactly one Event using the [defaultExecutionPolicy].
     *
     * @see addConsumer to return zero Events.
     * @see addValueCollector to emit more than one Event.
     *
     * @param effectHandler A suspending lambda that accepts the Effect and returns an Event.
     */
    public inline fun <reified G : F> addFunction(
        crossinline effectHandler: suspend (effect: G) -> E
    ) {
        addHandler(G::class, defaultExecutionPolicy) { emit(effectHandler(it)) }
    }

    /**
     * Receives one Effect and does not return any Events.
     *
     * @see addFunction to return one Event.
     * @see addValueCollector to emit more than one Event.
     *
     * @param executionPolicy The policy defining coroutines behavior.
     * @param effectHandler A suspending lambda that accepts the Effect and no Events.
     */
    public inline fun <reified G : F> addConsumer(
        executionPolicy: ExecutionPolicy,
        crossinline effectHandler: suspend (effect: G) -> Unit
    ) {
        addHandler(G::class, executionPolicy) { effectHandler(it) }
    }

    /**
     * Receives one Effect and does not return any Events using the [defaultExecutionPolicy].
     *
     * @see addFunction to return one Event.
     * @see addValueCollector to emit more than one Event.
     *
     * @param effectHandler A suspending lambda that accepts the Effect and no Events.
     */
    public inline fun <reified G : F> addConsumer(
        crossinline effectHandler: suspend (effect: G) -> Unit
    ) {
        addConsumer(defaultExecutionPolicy, effectHandler)
    }

    /**
     * Actions do not receive the Effect instant and do not return any Events.
     *
     * @see addConsumer to receive the Effect instance.
     * @see addFunction to receive the Effect instance and return one Event.
     * @see addValueCollector to emit more than one Event.
     *
     * @param executionPolicy The policy defining coroutines behavior.
     * @param effectHandler A suspending lambda with no arguments or return type.
     */
    public inline fun <reified G : F> addAction(
        executionPolicy: ExecutionPolicy,
        crossinline effectHandler: suspend () -> Unit
    ) {
        addHandler(G::class, executionPolicy) { effectHandler() }
    }

    /**
     * Actions do not receive the Effect instant and do not return any Events using the
     * [defaultExecutionPolicy].
     *
     * @see addConsumer to receive the Effect instance.
     * @see addFunction to receive the Effect instance and return one Event.
     * @see addValueCollector to emit more than one Event.
     *
     * @param effectHandler A suspending lambda with no arguments or return type.
     */
    public inline fun <reified G : F> addAction(
        crossinline effectHandler: suspend () -> Unit
    ) {
        addHandler(G::class, defaultExecutionPolicy) { effectHandler() }
    }

    /**
     * Using the [FlowCollector] builder lambda [effectHandler], receives an Effect
     * instance and allows emitting any number of Events.
     *
     * @param executionPolicy The policy defining coroutines behavior, defaults to [defaultExecutionPolicy].
     * @param effectHandler A `Flow` builder lambda.
     */
    public inline fun <reified G : F> addValueCollector(
        executionPolicy: ExecutionPolicy = defaultExecutionPolicy,
        crossinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) {
        addHandler(G::class, executionPolicy) { effectHandler(it) }
    }

    @Deprecated(
        "Use addValueCollector with the ExecutionPolicy.Latest policy.",
        ReplaceWith("addValueCollector<G>(ExecutionPolicy.Latest, effectHandler)")
    )
    public inline fun <reified G : F> addLatestValueCollector(
        crossinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) {
        addValueCollector<G>(ExecutionPolicy.Latest, effectHandler)
    }

    @PublishedApi
    internal fun <G : F> addFlowTransformer(
        effectClass: KClass<G>,
        attach: FlowTransformer<G, E>,
    ) {
        effectPerformerMap.keys.forEach { cls ->
            val assignable = cls == effectClass
            require(!assignable) {
                "Sub-type effect handler already registered for '${effectClass.simpleName}'"
            }
        }
        effectPerformerMap[effectClass] = FlowTransformer { effects ->
            val mappedEffects = effects
                .filter(effectClass::isInstance)
                .map(effectClass::cast)

            attach(mappedEffects).catch { e ->
                if (e !is CancellationException) {
                    throw UnrecoverableIncomingException(e)
                }
            }
        }
    }

    @PublishedApi
    internal fun <G : F> addHandler(
        effectClass: KClass<G>,
        executionPolicy: ExecutionPolicy,
        effectHandler: suspend FlowCollector<E>.(effect: G) -> Unit = NOOP_COLLECTOR
    ) {
        addFlowTransformer(effectClass) { effects ->
            executionPolicy.execute(effectHandler, effects)
        }
    }

    public fun build(): FlowTransformer<F, E> =
        MobiusEffectRouter(
            effectClasses = effectPerformerMap.keys.toSet(),
            effectPerformers = effectPerformerMap.values.toList()
        )
}
