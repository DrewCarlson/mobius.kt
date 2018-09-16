package com.spotify.mobius.runners

import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.TransferMode

actual object WorkRunners {

  actual fun immediate(): WorkRunner {
    return ImmediateWorkRunner()
  }

  fun nativeWorker(
      worker: Worker = Worker.start(),
      transferMode: TransferMode = TransferMode.SAFE,
      processScheduledJobs: Boolean = true
  ): WorkRunner {
    return NativeWorkRunner(
        worker,
        transferMode,
        processScheduledJobs
    )
  }
}
