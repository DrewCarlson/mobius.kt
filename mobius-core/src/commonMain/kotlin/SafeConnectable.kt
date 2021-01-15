package kt.mobius

import kt.mobius.disposables.CompositeDisposable
import kt.mobius.functions.Consumer

/**
 * A [Connectable] that ensures that an inner [Connection] doesn't emit or receive any
 * values after being disposed.
 *
 *
 * This only acts as a safeguard, you still need to make sure that the Connectable disposes of
 * resources correctly.
 */
class SafeConnectable<F, E>(
    private val actual: Connectable<F, E>
) : Connectable<F, E> {

    override fun connect(output: Consumer<E>): Connection<F> {
        val safeEventConsumer = SafeConsumer(output)
        val effectConsumer = SafeEffectConsumer(actual.connect(safeEventConsumer))
        val disposable = CompositeDisposable.from(safeEventConsumer, effectConsumer)
        return object : Connection<F> {
            override fun accept(effect: F): Unit = mpp.synchronized(this) {
                effectConsumer.accept(effect)
            }

            override fun dispose(): Unit = mpp.synchronized(this) {
                disposable.dispose()
            }
        }
    }

    class SafeEffectConsumer<F>(private val actual: Connection<F>) : Connection<F> {
        private object LOCK

        private var disposed: Boolean = false

        override fun accept(effect: F): Unit = mpp.synchronized(LOCK) {
            if (disposed) {
                return
            }
            actual.accept(effect)
        }

        override fun dispose(): Unit = mpp.synchronized(LOCK) {
            disposed = true
            actual.dispose()
        }
    }

    class SafeConsumer<E>(private val actual: Consumer<E>) : Connection<E> {
        private object LOCK

        private var disposed: Boolean = false

        override fun accept(value: E): Unit = mpp.synchronized(LOCK) {
            if (disposed) {
                return
            }
            actual.accept(value)
        }

        override fun dispose(): Unit = mpp.synchronized(LOCK) {
            disposed = true
        }
    }
}
