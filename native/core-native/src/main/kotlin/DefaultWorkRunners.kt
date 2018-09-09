package com.spotify.mobius.runners

import com.spotify.mobius.functions.Producer
import kotlin.native.concurrent.*

actual class DefaultWorkRunners {

  actual fun eventWorkRunnerProducer() = Producer<WorkRunner> {
    object : WorkRunner {
      val worker = Worker.start()
      override fun post(runnable: Runnable) {
        worker.execute(TransferMode.SAFE, { runnable }) { it.run() }
      }

      override fun dispose() {
        worker.requestTermination(false)
      }
    }
  }

  actual fun effectWorkRunnerProducer() = Producer<WorkRunner> {
    object : WorkRunner {
      val worker = Worker.start()
      override fun post(runnable: Runnable) {
        worker.execute(TransferMode.SAFE, { runnable }) { it.run() }
      }

      override fun dispose() {
        worker.requestTermination(false)
      }
    }
  }
}
