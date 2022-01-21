package kt.mobius.android

import androidx.lifecycle.Observer
import java.util.*
import kotlin.test.*

internal class RecordingObserver<V> : Observer<V> {
    private val values: MutableList<V> = ArrayList()
    private val lock = Any()

    override fun onChanged(value: V) {
        synchronized(lock) {
            values.add(value)
        }
    }

    fun valueCount(): Int {
        synchronized(lock) { return values.size }
    }

    fun assertValues(vararg expectedValues: V): Unit =
        kotlinx.atomicfu.locks.synchronized(lock) {
            assertEquals(expectedValues.asList(), values)
        }

    fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
        kotlinx.atomicfu.locks.synchronized(lock) {
            val missing = expectedValues.toSet() - values.toSet()
            assertTrue(values.containsAll(expectedValues.toList()), "Expected $missing")
        }

    fun clearValues() {
        synchronized(lock) { values.clear() }
    }
}
