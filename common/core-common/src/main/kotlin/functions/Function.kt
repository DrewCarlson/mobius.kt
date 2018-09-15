package com.spotify.mobius.functions


/** Interface for simple functions.  */
interface Function<T, R> {
  fun apply(value: T): R
}
