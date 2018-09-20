package com.spotify.mobius.functions


/** Interface for simple functions.  */
interface Function<T, R> {
  @mpp.JsName("apply")
  fun apply(value: T): R
}
