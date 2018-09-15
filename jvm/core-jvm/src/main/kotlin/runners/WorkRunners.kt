package com.spotify.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

actual object WorkRunners {

  actual fun immediate(): WorkRunner {
    return ImmediateWorkRunner()
  }

  fun singleThread(): WorkRunner {
    return from(Executors.newSingleThreadExecutor())
  }

  fun fixedThreadPool(n: Int): WorkRunner {
    return from(Executors.newFixedThreadPool(n))
  }

  fun cachedThreadPool(): WorkRunner {
    return from(Executors.newCachedThreadPool())
  }

  fun from(service: ExecutorService): WorkRunner {
    return ExecutorServiceWorkRunner(service)
  }
}
