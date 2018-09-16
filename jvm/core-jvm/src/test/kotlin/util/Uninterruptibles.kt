package com.spotify.mobius

import java.util.concurrent.TimeUnit

/**
 * Borrowed from Guava so we don't need it as a test dependency.
 * https://github.com/google/guava/blob/88e6fb86f5317bdfd2e8a78899334e9f0ba16987/guava/src/com/google/common/util/concurrent/Uninterruptibles.java
 */
object Uninterruptibles {

  fun sleepUninterruptibly(sleepFor: Long, unit: TimeUnit) {
    var interrupted = false
    try {
      var remainingNanos = unit.toNanos(sleepFor)
      val end = System.nanoTime() + remainingNanos
      while (true) {
        try {
          // TimeUnit.sleep() treats negative timeouts just like zero.
          TimeUnit.NANOSECONDS.sleep(remainingNanos)
          return
        } catch (e: InterruptedException) {
          interrupted = true
          remainingNanos = end - System.nanoTime()
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt()
      }
    }
  }
}
