package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public open class RecordingConsumer<V> : Consumer<V> {

    private val values = arrayListOf<V>()

    private val lock = SynchronizedObject()

    override fun accept(value: V): Unit =
        synchronized(lock) {
            values.add(value)
        }

    public fun valueCount(): Int =
        synchronized(lock) {
            values.size
        }

    public fun assertValues(vararg expectedValues: V): Unit =
        synchronized(lock) {
            assertEquals(expectedValues.asList(), values)
        }

    public fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
        synchronized(lock) {
            val missing = expectedValues.toSet() - values.toSet()
            assertTrue(values.containsAll(expectedValues.toList()), "Expected $missing")
        }

    public fun clearValues(): Unit =
        synchronized(lock) {
            values.clear()
        }
}
