package com.spotify.mobius.runners

import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Producer

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
interface WorkRunner : Disposable {
  fun post(runnable: Runnable)
}

expect interface Runnable {
  fun run()
}

/**
 * Provides the default Event and Effect work runners used by [Mobius.loop] when
 * configuring a default [MobiusLoop.Builder].
 */
expect class DefaultWorkRunners() {
  fun eventWorkRunnerProducer(): Producer<WorkRunner>
  fun effectWorkRunnerProducer(): Producer<WorkRunner>
}

/**
 * Interface for posting runnables to be executed on a thread.
 * The runnables must all be executed on the same thread for a given WorkRunner.
 */
object WorkRunners {

  fun immediate(): WorkRunner {
    return ImmediateWorkRunner()
  }
}
