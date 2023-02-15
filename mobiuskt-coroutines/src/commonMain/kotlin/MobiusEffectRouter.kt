package kt.mobius.flow

import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

internal class MobiusEffectRouter<F : Any, E>(
    private val effectClasses: Set<KClass<*>>,
    private val effectPerformers: List<FlowTransformer<F, E>>,
    private val ignoredEffects: List<KClass<*>>
) : FlowTransformer<F, E> {

    private val unhandledEffectHandler =
        flowTransformer<F, E> { effects: Flow<F> ->
            effects
                .filter { effect ->
                    ignoredEffects.none { effectClass ->
                        effectClass.isInstance(effect)
                    } && effectClasses.none { effectClass ->
                        effectClass.isInstance(effect)
                    }
                }
                .map { effect -> throw UnknownEffectException(effect) }
        }

    override fun invoke(input: Flow<F>): Flow<E> {
        return (effectPerformers + unhandledEffectHandler)
            .map { transform -> transform(input) }
            .merge()
    }
}

public data class UnknownEffectException(
    val effect: Any
) : RuntimeException() {
    override val message: String = effect.toString()
}
