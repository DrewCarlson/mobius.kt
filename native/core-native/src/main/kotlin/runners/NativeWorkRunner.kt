package com.spotify.mobius.runners

import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.TransferMode

class NativeWorkRunner internal constructor(
    private val worker: Worker = Worker.start(),
    private val transferMode: TransferMode = TransferMode.SAFE,
    private val processScheduledJobs: Boolean = true
) : WorkRunner {

  override fun post(runnable: Runnable) {
    worker.execute(transferMode, { runnable }) { it.run() }
  }

  override fun dispose() {
    worker.requestTermination(processScheduledJobs)
  }
}
