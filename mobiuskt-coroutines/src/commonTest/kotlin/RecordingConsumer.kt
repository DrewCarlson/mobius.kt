package kt.mobius.flow

import kotlinx.atomicfu.locks.SynchronizedObject
import kt.mobius.functions.Consumer
import mpp.synchronized
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("unused")
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
            assertTrue(values.containsAll(expectedValues.toList()))
        }

    fun clearValues(): Unit =
        synchronized(lock) {
            values.clear()
        }
}
