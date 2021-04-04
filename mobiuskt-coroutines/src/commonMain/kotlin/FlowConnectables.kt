package kt.mobius.flow

import kt.mobius.Connectable
import kt.mobius.Connection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Constructs a [Connectable] that applies [transform] to
 * map a [Flow] of [I] into a [Flow] of [O].
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun <I, O> flowConnectable(
    transform: FlowTransformer<I, O>
): Connectable<I, O> {
    val actual = Connectable<I, O> { consumer ->
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val inputChannel = BroadcastChannel<I>(BUFFERED)
        scope.launch {
            transform(inputChannel.asFlow()).collect { output ->
                ensureActive()
                consumer.accept(output)
            }
        }
        object : Connection<I> {
            override fun accept(value: I) {
                inputChannel.offer(value)
            }

            override fun dispose() {
                scope.cancel()
            }
        }
    }
    return DiscardAfterDisposeConnectable(actual)
}

/**
 * Transforms the [FlowTransformer] into a [Connectable].
 */
fun <I, O> FlowTransformer<I, O>.asConnectable(): Connectable<I, O> =
    flowConnectable(this)

/**
 * Transforms a [Flow] of [I] into a [Flow] of [O] using
 * the provided [Connectable] [connectable].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <I, O> Flow<I>.transform(
    connectable: Connectable<I, O>
): Flow<O> = callbackFlow {
    val connection = connectable.connect { output ->
        if (isActive) offer(output)
    }
    launch {
        onCompletion {
            close()
        }.collect { input ->
            ensureActive()
            connection.accept(input)
        }
    }

    awaitClose {
        connection.dispose()
    }
}
