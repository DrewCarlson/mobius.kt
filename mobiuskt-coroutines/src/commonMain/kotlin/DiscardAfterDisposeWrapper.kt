package kt.mobius.flow

import kt.mobius.Connection
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kotlin.jvm.Volatile

/**
 * Wraps a [Connection] or a [Consumer] and blocks them from receiving any
 * further values after the wrapper has been disposed.
 */
internal class DiscardAfterDisposeWrapper<I> private constructor(
        private val consumer: Consumer<I>,
        private val disposable: Disposable?
) : Consumer<I>, Disposable {

    @Volatile
    private var disposed: Boolean = false

    override fun accept(value: I) {
        if (disposed) {
            return
        }
        consumer.accept(value)
    }

    override fun dispose() {
        disposed = true
        disposable?.dispose()
    }

    companion object {
        fun <I> wrapConnection(connection: Connection<I>): DiscardAfterDisposeWrapper<I> {
            return DiscardAfterDisposeWrapper(connection, connection)
        }

        fun <I> wrapConsumer(consumer: Consumer<I>): DiscardAfterDisposeWrapper<I> {
            return DiscardAfterDisposeWrapper(consumer, null)
        }
    }
}
