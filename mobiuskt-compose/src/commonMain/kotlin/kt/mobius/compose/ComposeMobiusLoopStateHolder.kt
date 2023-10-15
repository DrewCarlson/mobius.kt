package kt.mobius.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State

@Immutable
public data class ComposeMobiusLoopStateHolder<M, E>(
    val model: State<M>,
    val eventConsumer: (E) -> Unit,
)
