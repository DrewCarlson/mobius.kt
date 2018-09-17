package com.spotify.mobius.test

import com.spotify.mobius.runners.WorkRunner
import com.spotify.mobius.runners.Runnable
import java.util.*

class TestWorkRunner : WorkRunner {

  private val queue = LinkedList<Runnable>()

  override fun post(runnable: Runnable) {
    synchronized(queue) {
      queue.add(runnable)
    }
  }

  private fun runOne() {
    lateinit var runnable: Runnable
    synchronized(queue) {
      if (queue.isEmpty()) return
      runnable = queue.remove()
    }
    runnable.run()
  }

  fun runAll() {
    while (true) {
      synchronized(queue) {
        if (queue.isEmpty()) return
      }
      runOne()
    }
  }

  override fun dispose() {
    synchronized(queue) {
      queue.clear()
    }
  }
}
