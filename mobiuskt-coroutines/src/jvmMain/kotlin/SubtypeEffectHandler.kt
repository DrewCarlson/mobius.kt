package kt.mobius.flow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.invoke

fun <F : Any, E> subtypeEffectHandler(
    block: SubtypeEffectHandlerBuilder<F, E>.() -> Unit
): FlowTransformer<F, E> =
    SubtypeEffectHandlerBuilder<F, E>()
        .apply(block)
        .build()

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SubtypeEffectHandlerBuilder<F : Any, E> {
    private val effectPerformerMap = hashMapOf<Class<*>, FlowTransformer<F, E>>()

    inline fun <reified G : F> addTransformer(
        noinline effectHandler: FlowTransformer<G, E>
    ) = addTransformer(G::class.java, effectHandler)

    inline fun <reified G : F> addFunction(
        noinline effectHandler: suspend (effect: G) -> E
    ) = addFunction(G::class.java, effectHandler)

    inline fun <reified G : F> addConsumer(
        noinline effectHandler: suspend (effect: G) -> Unit
    ) = addConsumer(G::class.java, effectHandler)

    inline fun <reified G : F> addAction(
        noinline effectHandler: suspend () -> Unit
    ) = addAction(G::class.java, effectHandler)

    inline fun <reified G : F> addValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) = addTransformer(G::class.java) { it.transform(effectHandler) }

    inline fun <reified G : F> addLatestValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) = addTransformer(G::class.java) { it.transformLatest(effectHandler) }

    inline fun <reified G : F> addFunctionSync(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        crossinline effectHandler: (effect: G) -> E
    ) = addFunction(G::class.java) { effect ->
        dispatcher { effectHandler(effect) }
    }

    inline fun <reified G : F> addConsumerSync(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        crossinline effectHandler: (effect: G) -> Unit
    ) = addConsumer(G::class.java) { effect ->
        dispatcher { effectHandler(effect) }
    }

    inline fun <reified G : F> addActionSync(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        crossinline effectHandler: () -> Unit
    ) = addAction(G::class.java) {
        dispatcher { effectHandler() }
    }

    fun <G : F> addTransformer(
        effectClass: Class<G>,
        effectHandler: FlowTransformer<G, E>
    ): SubtypeEffectHandlerBuilder<F, E> {
        effectPerformerMap
            .keys
            .forEach { cls ->
                val assignable = cls.isAssignableFrom(effectClass) ||
                    effectClass.isAssignableFrom(cls)
                check(!assignable) {
                    "Sub-type effect handler already registered for '${effectClass.name}'"
                }
            }
        effectPerformerMap[effectClass] = { effects ->
            effects
                .filter { effect -> effectClass.isInstance(effect) }
                .map { effect -> effectClass.cast(effect) }
                .run(effectHandler)
            // TODO: .catch unhandled exceptions
        }

        return this
    }

    fun <G : F> addFunction(
        effectClass: Class<G>,
        effectHandler: suspend (effect: G) -> E
    ) = addTransformer(effectClass) { effects ->
        effects.map(effectHandler)
    }

    fun <G : F> addConsumer(
        effectClass: Class<G>,
        effectHandler: suspend (effect: G) -> Unit
    ) = addTransformer(effectClass) { effects ->
        effects.transform { effect -> effectHandler(effect) }
    }

    fun <G : F> addAction(
        effectClass: Class<G>,
        effectHandler: suspend () -> Unit
    ) = addTransformer(effectClass) { effects ->
        effects.transform { effectHandler() }
    }

    fun build(): FlowTransformer<F, E> =
        MobiusEffectRouter(
            effectClasses = effectPerformerMap.keys.toSet(),
            effectPerformers = effectPerformerMap.values.toList()
        )
}
