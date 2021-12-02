package kt.mobius.flow

import kt.mobius.functions.Consumer
import mpp.synchronized
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordingConsumer<V> : Consumer<V> {

    private val values = arrayListOf<V>()

    private object LOCK

    override fun accept(value: V): Unit =
        synchronized(LOCK) {
            values.add(value)
        }

    fun valueCount(): Int =
        synchronized(LOCK) {
            values.size
        }

    fun assertValues(vararg expectedValues: V): Unit =
        synchronized(LOCK) {
            assertEquals(expectedValues.asList(), values)
        }

    fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
        synchronized(LOCK) {
            assertTrue(values.containsAll(expectedValues.toList()))
        }

    fun clearValues(): Unit =
        synchronized(LOCK) {
            values.clear()
        }
}
