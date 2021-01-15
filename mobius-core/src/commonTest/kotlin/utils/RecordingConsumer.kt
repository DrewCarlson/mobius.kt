package kt.mobius

import kt.mobius.functions.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordingConsumer<V> : Consumer<V> {

    private val values = arrayListOf<V>()

    private object LOCK

    override fun accept(value: V): Unit =
        mpp.synchronized(LOCK) {
            values.add(value)
        }

    fun valueCount(): Int =
        mpp.synchronized(LOCK) {
            values.size
        }

    fun assertValues(vararg expectedValues: V): Unit =
        mpp.synchronized(LOCK) {
            assertEquals(values, expectedValues.asList())
        }

    fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
        mpp.synchronized(LOCK) {
            assertTrue(values.containsAll(expectedValues.toList()))
        }

    fun clearValues(): Unit =
        mpp.synchronized(LOCK) {
            values.clear()
        }
}
