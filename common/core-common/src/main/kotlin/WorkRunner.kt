package com.spotify.mobius.runners

import com.spotify.mobius.disposables.Disposable

/** Interface for posting runnables to be executed using different scheduling mechanisms.  */
interface WorkRunner : Disposable {
  fun post(runnable: Runnable)
}

expect interface Runnable {
  fun run()
}
