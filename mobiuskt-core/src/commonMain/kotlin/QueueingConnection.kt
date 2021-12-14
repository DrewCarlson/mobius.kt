package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Provides a connection that queues up messages until a delegate to consume them is available.
 * Useful for setting up circular dependencies safely. All methods are synchronized for ease of
 * implementation.
 */
internal class QueuingConnection<I> : Connection<I> {
    private val lock = object : SynchronizedObject() {}

    private val queue: MutableList<I> = arrayListOf()
    private var delegate: Connection<I>? = null
    private var disposed = false

    fun setDelegate(delegate: Connection<I>) {
        synchronized(lock) {
            check(this.delegate == null) { "Attempt at setting delegate twice" }

            this.delegate = delegate

            if (disposed) return

            for (item in queue) {
                delegate.accept(item)
            }
            queue.clear()
        }
    }

    override fun accept(value: I) {
        synchronized(lock) {
            delegate?.run {
                accept(value)
                return
            }

            queue.add(value)
        }
    }

    override fun dispose() {
        synchronized(lock) {
            disposed = true
            delegate?.dispose()
        }
    }
}