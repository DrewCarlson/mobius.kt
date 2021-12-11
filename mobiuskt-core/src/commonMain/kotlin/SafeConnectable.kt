package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kt.mobius.disposables.CompositeDisposable
import kt.mobius.functions.Consumer
import mpp.synchronized

/**
 * A [Connectable] that ensures that an inner [Connection] doesn't emit or receive any
 * values after being disposed.
 *
 *
 * This only acts as a safeguard, you still need to make sure that the Connectable disposes of
 * resources correctly.
 */
public class SafeConnectable<F, E>(
    private val actual: Connectable<F, E>
) : Connectable<F, E> {

    override fun connect(output: Consumer<E>): Connection<F> {
        val safeEventConsumer = SafeConsumer(output)
        val effectConsumer = SafeEffectConsumer(actual.connect(safeEventConsumer))
        val disposable = CompositeDisposable.from(safeEventConsumer, effectConsumer)
        return object : SynchronizedObject(), Connection<F> {
            override fun accept(value: F): Unit = synchronized(this) {
                effectConsumer.accept(value)
            }

            override fun dispose(): Unit = synchronized(this) {
                disposable.dispose()
            }
        }
    }

    public class SafeEffectConsumer<F>(private val actual: Connection<F>) : Connection<F> {
        private val lock = object : SynchronizedObject() {}

        private var disposed: Boolean = false

        override fun accept(value: F): Unit = synchronized(lock) {
            if (disposed) {
                return
            }
            actual.accept(value)
        }

        override fun dispose(): Unit = synchronized(lock) {
            disposed = true
            actual.dispose()
        }
    }

    public class SafeConsumer<E>(private val actual: Consumer<E>) : Connection<E> {
        private val lock = object : SynchronizedObject() {}

        private var disposed: Boolean = false

        override fun accept(value: E): Unit = synchronized(lock) {
            if (disposed) {
                return
            }
            actual.accept(value)
        }

        override fun dispose(): Unit = synchronized(lock) {
            disposed = true
        }
    }
}
