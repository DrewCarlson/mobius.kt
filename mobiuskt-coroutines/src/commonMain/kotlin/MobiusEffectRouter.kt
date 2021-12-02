package kt.mobius.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class MobiusEffectRouter<F : Any, E>(
    private val effectClasses: Set<KClass<*>>,
    private val effectPerformers: List<FlowTransformer<F, E>>
) : FlowTransformer<F, E> {

    private val unhandledEffectHandler =
        flowTransformer<F, E> { effects: Flow<F> ->
            effects
                .filter { effect ->
                    effectClasses
                        .none { effectClass ->
                            effectClass.isInstance(effect)
                        }
                }
                .map { effect -> throw UnknownEffectException(effect) }
        }

    override fun invoke(input: Flow<F>): Flow<E> {
        val scope = CoroutineScope(EmptyCoroutineContext)
        val effectFlow = input.shareIn(scope, SharingStarted.Eagerly)
        return (effectPerformers + unhandledEffectHandler)
            .map { transform -> transform(effectFlow) }
            .merge()
    }
}

data class UnknownEffectException(
    val effect: Any
) : RuntimeException() {
    override val message = effect.toString()
}