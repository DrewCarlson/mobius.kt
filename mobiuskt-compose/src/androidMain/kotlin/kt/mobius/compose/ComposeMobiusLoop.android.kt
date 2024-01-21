package kt.mobius.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kt.mobius.*
import kt.mobius.android.MobiusLoopFactoryProvider
import kt.mobius.android.MobiusLoopViewModel
import kt.mobius.functions.Consumer

@ExperimentalMobiusktComposeApi
@Composable
public actual fun <M, E, F> rememberMobiusLoop(
    startModel: M,
    init: Init<M, F>?,
    loopBuilder: () -> MobiusLoop.Builder<M, E, F>
): ComposeMobiusLoopStateHolder<M, E> {
    val storeOwner = LocalViewModelStoreOwner.current
    return if (storeOwner is Activity) {
        rememberMobiusLoopLocal(
            startModel = startModel,
            init = init,
            loopBuilder = loopBuilder,
        )
    } else {
        rememberMobiusLoopViewModel(
            startModel = startModel,
            init = init,
            loopBuilder = { _, _ -> loopBuilder() },
        )
    }
}

/**
 * A Mobius Loop lifecycle handler stored in [MobiusLoopViewModel], supporting Jetpack Navigation.
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
@Composable
public fun <M, E, F, V : F> rememberMobiusLoopViewModel(
    startModel: M,
    init: Init<M, F>? = null,
    loopBuilder: MobiusLoopFactoryProvider<M, E, F, V>
): ComposeMobiusLoopStateHolder<M, E> {
    val viewModel = viewModel {
        MobiusLoopViewModel.create(
            loopFactoryProvider = loopBuilder,
            init = init,
            modelToStartFrom = startModel
        )
    }
    val models = viewModel.models.observeAsState(viewModel.model)

    return remember {
        ComposeMobiusLoopStateHolder(
            model = models,
            eventConsumer = viewModel::dispatchEvent,
        )
    }
}
