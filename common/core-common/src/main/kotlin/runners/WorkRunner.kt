package com.spotify.mobius.runners

import com.spotify.mobius.disposables.Disposable

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
interface WorkRunner : Disposable {
  companion object {
    operator fun invoke(post: (Runnable) -> Unit): WorkRunner {
      return object : WorkRunner {
        override fun post(runnable: Runnable) {
          post(runnable)
        }

        override fun dispose() {
        }
      }
    }
  }

  fun post(runnable: Runnable)
}
