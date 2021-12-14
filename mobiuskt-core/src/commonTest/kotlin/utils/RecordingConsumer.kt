package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordingConsumer<V> : Consumer<V> {

    private val values = arrayListOf<V>()

    private val lock = object : SynchronizedObject() {}

    override fun accept(value: V): Unit =
        synchronized(lock) {
            values.add(value)
        }

    fun valueCount(): Int =
        synchronized(lock) {
            values.size
        }

    fun assertValues(vararg expectedValues: V): Unit =
        synchronized(lock) {
            assertEquals(expectedValues.asList(), values)
        }

    fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
        synchronized(lock) {
            val missing = expectedValues.toSet() - values.toSet()
            assertTrue(values.containsAll(expectedValues.toList()), "Expected $missing")
        }

    fun clearValues(): Unit =
        synchronized(lock) {
            values.clear()
        }
}
