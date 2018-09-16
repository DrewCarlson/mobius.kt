package com.spotify.mobius.runners

import com.spotify.mobius.functions.Producer

actual class DefaultWorkRunners {

  actual fun eventWorkRunnerProducer() =
      Producer<WorkRunner> {
        WorkRunners.nativeWorker()
      }

  actual fun effectWorkRunnerProducer() =
      Producer<WorkRunner> {
        WorkRunners.nativeWorker()
      }
}
