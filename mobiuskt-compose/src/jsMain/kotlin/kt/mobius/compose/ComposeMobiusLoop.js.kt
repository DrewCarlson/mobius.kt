package kt.mobius.compose

import androidx.compose.runtime.Composable
import kt.mobius.Init
import kt.mobius.MobiusLoop

@ExperimentalMobiusktComposeApi
@Composable
public actual fun <M, E, F> rememberMobiusLoop(
    startModel: M,
    init: Init<M, F>?,
    loopBuilder: () -> MobiusLoop.Builder<M, E, F>
): ComposeMobiusLoopStateHolder<M, E> {
    return rememberMobiusLoopLocal(
        startModel = startModel,
        init = init,
        loopBuilder = loopBuilder,
    )
}
