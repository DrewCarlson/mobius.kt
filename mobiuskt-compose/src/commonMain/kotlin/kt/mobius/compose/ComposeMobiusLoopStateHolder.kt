package kt.mobius.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import kt.mobius.functions.Consumer

/**
 * Holds the [State]<[M]> and [Consumer]<[E]> created by [rememberMobiusLoop].
 *
 * For easy access, the [model] state and [eventConsumer] can be destructured:
 * ```kotlin
 * val (modelState, eventConsumer) = rememberMobiusLoop(Model()) {
 *      Mobius.loop(Update(), effectHandler)
 * }
 * ```
 */
@ExperimentalMobiusktComposeApi
@Immutable
public data class ComposeMobiusLoopStateHolder<M, E>(
    val model: State<M>,
    val eventConsumer: (E) -> Unit,
)
