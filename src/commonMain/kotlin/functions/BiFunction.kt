package com.spotify.mobius.functions


/** Interface for simple functions with two arguments.  */
interface BiFunction<T, U, R> {
  fun apply(value1: T, value2: U): R
}
