package kt.mobius.flow

import kt.mobius.Connectable
import kt.mobius.Connection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kt.mobius.DiscardAfterDisposeConnectable

/**
 * Constructs a [Connectable] that applies [transform] to
 * map a [Flow] of [I] into a [Flow] of [O].
 */
@OptIn(ExperimentalCoroutinesApi::class)
public fun <I, O> flowConnectable(
    transform: FlowTransformer<I, O>
): Connectable<I, O> {
    val actual = Connectable<I, O> { consumer ->
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val inputChannel = MutableSharedFlow<I>(
                extraBufferCapacity = Int.MAX_VALUE,
                onBufferOverflow = BufferOverflow.SUSPEND,
        )
        scope.launch(start = CoroutineStart.ATOMIC) {
            transform(inputChannel)
                .onEach { output ->
                    ensureActive()
                    consumer.accept(output)
                }
                .collect()
        }
        object : Connection<I> {
            override fun accept(value: I) {
                inputChannel.tryEmit(value)
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
public fun <I, O> FlowTransformer<I, O>.asConnectable(): Connectable<I, O> =
    flowConnectable(this)

/**
 * Transforms a [Flow] of [I] into a [Flow] of [O] using
 * the provided [Connectable] [connectable].
 */
public fun <I, O> Flow<I>.transform(
    connectable: Connectable<I, O>
): Flow<O> = callbackFlow {
    val connection = connectable.connect { output ->
        if (isActive) trySend(output)
    }
    launch {
        onEach { input ->
            ensureActive()
            connection.accept(input)
        }.onCompletion {
            close()
        }.collect()
    }

    awaitClose {
        connection.dispose()
    }
}
