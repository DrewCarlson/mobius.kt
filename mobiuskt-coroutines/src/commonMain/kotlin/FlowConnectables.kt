package kt.mobius.flow

import kt.mobius.Connectable
import kt.mobius.Connection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Constructs a [Connectable] that applies [transform] to
 * map a [Flow] of [I] into a [Flow] of [O].
 */
public fun <I, O> flowConnectable(
    transform: FlowTransformer<I, O>
): Connectable<I, O> {
    val actual = Connectable<I, O> { consumer ->
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val inputChannel = MutableSharedFlow<I>(
                extraBufferCapacity = 64,
                onBufferOverflow = BufferOverflow.SUSPEND,
        )
        scope.launch {
            transform(inputChannel).collect { output ->
                ensureActive()
                consumer.accept(output)
            }
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
@OptIn(ExperimentalCoroutinesApi::class)
public fun <I, O> Flow<I>.transform(
    connectable: Connectable<I, O>
): Flow<O> = callbackFlow {
    val connection = connectable.connect { output ->
        if (isActive) trySend(output)
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
