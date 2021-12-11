package kt.mobius.extras

import kotlinx.atomicfu.locks.SynchronizedObject
import kt.mobius.functions.Consumer
import mpp.synchronized

/**
 * Collects events and passes them in order to a new consumer via [dequeueAll].
 */
@Suppress("unused")
class QueuedConsumer<V> : Consumer<V> {

    private val lock = object : SynchronizedObject() {}

    private val queue = arrayListOf<V>()

    override fun accept(value: V) = synchronized<Unit>(lock) {
        queue.add(value)
    }

    fun dequeueAll(target: Consumer<V>) = synchronized<Unit>(lock) {
        queue.forEach(target::accept)
        queue.clear()
    }
}
