package com.spotify.mobius.internal_util

/** Utilities for working with throwables.  */
object Throwables {

  fun propagate(e: Exception): RuntimeException {
    if (e is RuntimeException) {
      throw e
    }

    throw RuntimeException(e)
  }
}
