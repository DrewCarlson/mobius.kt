package kt.mobius.compose

import androidx.compose.runtime.*
import kt.mobius.*
import kt.mobius.First.Companion.first
import kt.mobius.LoggingInit
import kt.mobius.functions.Consumer

@Composable
public fun <M, E, F> rememberMobiusLoop(
    startModel: M,
    init: Init<M, F>? = null,
    loopBuilder: () -> MobiusLoop.Builder<M, E, F>
): ComposeMobiusLoopStateHolder<M, E> {
    return rememberMobiusLoopInternal(
        startModel = startModel,
        init = init,
        loopBuilder = loopBuilder,
    )
}

@Composable
private fun <M, E, F> rememberMobiusLoopInternal(
    startModel: M,
    init: Init<M, F>? = null,
    loopBuilder: () -> MobiusLoop.Builder<M, E, F>
): ComposeMobiusLoopStateHolder<M, E> {
    val loopFactory = remember(loopBuilder) { loopBuilder() }
    val first = remember {
        val actualInit = init ?: Init { first(startModel) }
        LoggingInit.fromLoop(actualInit, loopFactory)
            .init(startModel)
    }
    val mobiusLoop = remember(loopBuilder) {
        loopBuilder()
            .startFrom(first.model(), first.effects())
    }
    val modelState = remember { mutableStateOf(first.model()) }
    val eventConsumer = remember { mutableStateOf<Consumer<E>>(NoopConsumer()) }

    DisposableEffect(mobiusLoop) {
        val observerDisposable = mobiusLoop.observe { model ->
            modelState.value = model
            eventConsumer.value = Consumer(mobiusLoop::dispatchEvent)
            object : Connection<M> {
                override fun accept(value: M) {
                    modelState.value = value
                }

                override fun dispose() {
                    eventConsumer.value = NoopConsumer()
                }
            }
        }

        onDispose(observerDisposable::dispose)
    }
    return remember {
        ComposeMobiusLoopStateHolder(
            model = modelState,
            eventConsumer = { event -> eventConsumer.value.accept(event) },
        )
    }
}
