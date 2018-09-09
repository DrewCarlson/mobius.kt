package com.spotify.mobius.functions

/** Interface for consuming values. */
interface Consumer<V> {
  companion object {
    operator fun <V> invoke(accept: (@ParameterName("value") V) -> Unit): Consumer<V> {
      return object : Consumer<V> {
        override fun accept(value: V) = accept(value)
      }
    }
  }
  fun accept(value: V)
}

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

/** Interface for simple functions with two arguments.  */
interface BiFunction<T, U, R> {
  fun apply(value1: T, value2: U): R
}

/** Interface for simple functions.  */
interface Function<T, R> {
  fun apply(value: T): R
}
