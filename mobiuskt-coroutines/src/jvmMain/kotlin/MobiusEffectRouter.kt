package kt.mobius.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MobiusEffectRouter<F : Any, E>(
    private val effectClasses: Set<Class<*>>,
    private val effectPerformers: List<FlowTransformer<F, E>>
) : FlowTransformer<F, E> {

    private val unhandledEffectHandler =
        flowTransformer<F, E> { effects ->
            effects
                .filter { effect ->
                    effectClasses
                        .none { effectClass ->
                            effectClass.isAssignableFrom(effect::class.java)
                        }
                }
                .map { effect -> throw UnknownEffectException(effect) }
        }

    override fun invoke(effects: Flow<F>): Flow<E> {
        val scope = CoroutineScope(EmptyCoroutineContext)
        val effectFlow = effects.shareIn(scope, SharingStarted.Eagerly)
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
