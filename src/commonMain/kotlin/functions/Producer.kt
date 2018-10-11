package com.spotify.mobius.functions


/** Interface for producing values. */
interface Producer<V> {
  companion object {
    operator fun <V> invoke(get: () -> V): Producer<V> {
      return object : Producer<V> {
        override fun get() = get()
      }
    }
  }
  fun get(): V
}
