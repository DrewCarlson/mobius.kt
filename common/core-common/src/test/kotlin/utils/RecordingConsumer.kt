package com.spotify.mobius

import com.spotify.mobius.functions.Consumer
import synchronized2
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class RecordingConsumer<V> : Consumer<V> {

  private val values = arrayListOf<V>()

  private object LOCK

  override fun accept(value: V): Unit =
      synchronized2(LOCK) {
        values.add(value)
      }

  fun valueCount(): Int =
      synchronized2(LOCK) {
        values.size
      }

  fun assertValues(vararg expectedValues: V): Unit =
      synchronized2(LOCK) {
        assertEquals(values, expectedValues.asList())
      }

  fun assertValuesInAnyOrder(vararg expectedValues: V): Unit =
      synchronized2(LOCK) {
        assertTrue(values.containsAll(expectedValues.toList()))
      }

  fun clearValues(): Unit =
      synchronized2(LOCK) {
        values.clear()
      }
}
