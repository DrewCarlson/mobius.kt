package kt.mobius.compose

import androidx.compose.runtime.*
import kt.mobius.*
import kt.mobius.First.Companion.first
import kt.mobius.LoggingInit
import kt.mobius.functions.Consumer

/**
 * A Mobius Loop lifecycle handler bound to a [Composable] function.
 *
 * The loop will be started immediately with [startModel].
 * To restore state from a previous loop instance,
 * use a storage mechanism the platform provides to persist
 * the model instance, then read it into [startModel].
 *
 * Create the loop outside the main Composable UI function.
 * You should provide the model and event consumer function as parameters
 * to maintain preview support.
 * The loop setup would typically live at the same level as your navigation
 * handler body for the associated route.
 *
 * @param M The Model with which the Mobius Loop will run
 * @param E The Event type accepted by the loop
 * @param F The Effect type handled by the loop
 * @param startModel The initial model used when starting the loop.
 * @param init The [Init] function to use when starting the loop
 * @param loopBuilder A lambda which produces the loop builder to use.
 *
 * @return An object holding the [State]<[M]> and [Consumer]<[E]>, destructure in that order.
 */
@ExperimentalMobiusktComposeApi
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
    val first = remember(loopFactory) {
        val actualInit = init ?: Init { first(startModel) }
        LoggingInit.fromLoop(actualInit, loopFactory)
            .init(startModel)
    }
    val mobiusLoop = remember(loopFactory) {
        loopFactory.startFrom(first.model(), first.effects())
    }
    val modelState = remember { mutableStateOf(mobiusLoop.mostRecentModel) }
    val eventConsumer = remember { mutableStateOf<Consumer<E>?>(null) }

    DisposableEffect(mobiusLoop) {
        mobiusLoop.observe { model ->
            modelState.value = model
            eventConsumer.value = Consumer(mobiusLoop::dispatchEvent)
            object : Connection<M> {
                override fun accept(value: M) {
                    modelState.value = value
                }

                override fun dispose() {
                    eventConsumer.value = null
                }
            }
        }

        onDispose(mobiusLoop::dispose)
    }
    return remember {
        ComposeMobiusLoopStateHolder(
            model = modelState,
            eventConsumer = { event -> eventConsumer.value?.accept(event) },
        )
    }
}
