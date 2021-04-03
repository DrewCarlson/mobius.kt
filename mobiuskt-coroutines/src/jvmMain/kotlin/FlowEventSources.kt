package kt.mobius.flow

import kt.mobius.EventSource
import kt.mobius.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Create an [EventSource] that emits each [E] from this [Flow].
 *
 * Collection of the flow will be launched in [scope] when
 * [EventSource.subscribe] is called and the job cancelled when
 * on [Disposable.dispose].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <E> Flow<E>.toEventSource(
    scope: CoroutineScope
): EventSource<E> =
    EventSource { output ->
        val job = onEach { event ->
            output.accept(event)
        }.launchIn(scope)

        Disposable { job.cancel() }
    }

/**
 * Create a [Flow] that emits each [E] from this [EventSource].
 *
 * [EventSource.subscribe] is called when collection begins and
 * [Disposable.dispose] is called when the [Flow] is cancelled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <E> EventSource<E>.toFlow(): Flow<E> =
    callbackFlow {
        val disposable = subscribe { event -> offer(event) }

        awaitClose { disposable.dispose() }
    }
