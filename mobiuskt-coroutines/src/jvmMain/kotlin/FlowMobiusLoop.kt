package kt.mobius.flow

import kt.mobius.MobiusLoop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowMobiusLoop<M, E> internal constructor(
    private val loopFactory: MobiusLoop.Factory<M, E, *>,
    private val startModel: M
) : FlowTransformer<E, M> {

    override fun invoke(events: Flow<E>): Flow<M> =
        callbackFlow {
            val loop = loopFactory.startFrom(startModel)

            loop.observe { newModel -> offer(newModel) }

            events
                .onEach { event -> loop.dispatchEvent(event) }
                .catch { e -> throw UnrecoverableIncomingException(e) }
                .launchIn(this)

            awaitClose { loop.dispose() }
        }
}
