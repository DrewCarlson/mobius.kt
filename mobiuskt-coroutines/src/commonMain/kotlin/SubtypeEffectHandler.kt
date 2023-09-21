package kt.mobius.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.transform
import kotlin.reflect.KClass
import kotlin.reflect.cast

public fun <F : Any, E> subtypeEffectHandler(
    block: SubtypeEffectHandlerBuilder<F, E>.() -> Unit
): FlowTransformer<F, E> =
    SubtypeEffectHandlerBuilder<F, E>()
        .apply(block)
        .build()

private val NOOP_ACTION = suspend {}

public class SubtypeEffectHandlerBuilder<F : Any, E> {
    private val effectPerformerMap = hashMapOf<KClass<*>, FlowTransformer<F, E>>()

    public inline fun <reified G : F> ignore() { addAction(G::class) }

    public inline fun <reified G : F> addTransformer(
        noinline effectHandler: (input: Flow<G>) -> Flow<E>
    ) { addTransformer(G::class, effectHandler) }

    public inline fun <reified G : F> addFunction(
        noinline effectHandler: suspend (effect: G) -> E
    ) { addFunction(G::class, effectHandler) }

    public inline fun <reified G : F> addConsumer(
        noinline effectHandler: suspend (effect: G) -> Unit
    ) { addConsumer(G::class, effectHandler) }

    public inline fun <reified G : F> addAction(
        noinline effectHandler: suspend () -> Unit
    ) { addAction(G::class, effectHandler) }

    public inline fun <reified G : F> addValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) { addTransformer(G::class) { it.transform(effectHandler) } }

    @OptIn(ExperimentalCoroutinesApi::class)
    public inline fun <reified G : F> addLatestValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) { addTransformer(G::class) { it.transformLatest(effectHandler) } }

    @PublishedApi
    internal fun <G : F> addTransformer(
        effectClass: KClass<G>,
        effectHandler: FlowTransformer<G, E>
    ) {
        effectPerformerMap.keys.forEach { cls ->
            val assignable = cls == effectClass
            require(!assignable) {
                "Sub-type effect handler already registered for '${effectClass.simpleName}'"
            }
        }
        effectPerformerMap[effectClass] = FlowTransformer { effects ->
            effects
                .filter(effectClass::isInstance)
                .map(effectClass::cast)
                .run(effectHandler::invoke)
                .catch { e ->
                    if (e !is CancellationException) {
                        throw UnrecoverableIncomingException(e)
                    }
                }
        }
    }

    @PublishedApi
    internal fun <G : F> addFunction(
        effectClass: KClass<G>,
        effectHandler: suspend (effect: G) -> E
    ) {
        addTransformer(effectClass) { effects ->
            effects.map(effectHandler)
        }
    }

    @PublishedApi
    internal fun <G : F> addConsumer(
        effectClass: KClass<G>,
        effectHandler: suspend (effect: G) -> Unit
    ) {
        addTransformer(effectClass) { effects ->
            effects.transform { effect -> effectHandler(effect) }
        }
    }

    @PublishedApi
    internal fun <G : F> addAction(
        effectClass: KClass<G>,
        effectHandler: suspend () -> Unit = NOOP_ACTION
    ) {
        addTransformer(effectClass) { effects ->
            effects.transform { effectHandler() }
        }
    }

    public fun build(): FlowTransformer<F, E> =
        MobiusEffectRouter(
            effectClasses = effectPerformerMap.keys.toSet(),
            effectPerformers = effectPerformerMap.values.toList()
        )
}
