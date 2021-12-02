package kt.mobius.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.transform
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.typeOf

fun <F : Any, E> subtypeEffectHandler(
    block: SubtypeEffectHandlerBuilder<F, E>.() -> Unit
): FlowTransformer<F, E> =
    SubtypeEffectHandlerBuilder<F, E>()
        .apply(block)
        .build()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified T : Any> extractKClass(): KClass<T> {
    return typeOf<T>().classifier as KClass<T>
}

class SubtypeEffectHandlerBuilder<F : Any, E> {
    private val effectPerformerMap = hashMapOf<KClass<*>, FlowTransformer<F, E>>()

    inline fun <reified G : F> addTransformer(
        noinline effectHandler: (input: Flow<G>) -> Flow<E>
    ) = addTransformer(extractKClass<G>(), effectHandler)

    inline fun <reified G : F> addFunction(
        noinline effectHandler: suspend (effect: G) -> E
    ) = addFunction(extractKClass<G>(), effectHandler)

    inline fun <reified G : F> addConsumer(
        noinline effectHandler: suspend (effect: G) -> Unit
    ) = addConsumer(extractKClass<G>(), effectHandler)

    inline fun <reified G : F> addAction(
        noinline effectHandler: suspend () -> Unit
    ) = addAction(extractKClass<G>(), effectHandler)

    inline fun <reified G : F> addValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) = addTransformer(extractKClass<G>()) { it.transform(effectHandler) }

    @OptIn(ExperimentalCoroutinesApi::class)
    inline fun <reified G : F> addLatestValueCollector(
        noinline effectHandler: suspend FlowCollector<E>.(G) -> Unit
    ) = addTransformer(extractKClass<G>()) { it.transformLatest(effectHandler) }

    fun <G : F> addTransformer(
        effectClass: KClass<G>,
        effectHandler: FlowTransformer<G, E>
    ): SubtypeEffectHandlerBuilder<F, E> {
        effectPerformerMap
            .keys
            .forEach { cls ->
                val assignable = cls.isInstance(effectClass) || effectClass.isInstance(cls)
                check(!assignable) {
                    "Sub-type effect handler already registered for '${effectClass.simpleName}'"
                }
            }
        effectPerformerMap[effectClass] = FlowTransformer { effects ->
            effects
                .filter { effect -> effectClass.isInstance(effect) }
                .map { effect -> effectClass.cast(effect) }
                .run(effectHandler::invoke)
                .catch { throw UnrecoverableIncomingException(it) }
        }

        return this
    }

    fun <G : F> addFunction(
        effectClass: KClass<G>,
        effectHandler: suspend (effect: G) -> E
    ) = addTransformer(effectClass) { effects ->
        effects.map(effectHandler)
    }

    fun <G : F> addConsumer(
        effectClass: KClass<G>,
        effectHandler: suspend (effect: G) -> Unit
    ) = addTransformer(effectClass) { effects ->
        effects.transform { effect -> effectHandler(effect) }
    }

    fun <G : F> addAction(
        effectClass: KClass<G>,
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
