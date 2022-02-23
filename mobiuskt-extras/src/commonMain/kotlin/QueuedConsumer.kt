package kt.mobius.extras

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer
import kotlin.js.JsExport

/**
 * Collects events and passes them in order to a new consumer via [dequeueAll].
 */
@Suppress("unused", "NON_EXPORTABLE_TYPE")
@JsExport
public class QueuedConsumer<V> : Consumer<V> {

    private val lock = SynchronizedObject()

    private val queue = arrayListOf<V>()

    override fun accept(value: V): Unit = synchronized(lock) {
        queue.add(value)
    }

    public fun dequeueAll(target: Consumer<V>): Unit = synchronized(lock) {
        queue.forEach(target::accept)
        queue.clear()
    }
}
