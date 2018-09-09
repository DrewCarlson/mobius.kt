package com.spotify.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun WorkRunners.singleThread(): WorkRunner {
  return from(Executors.newSingleThreadExecutor())
}

fun WorkRunners.fixedThreadPool(n: Int): WorkRunner {
  return from(Executors.newFixedThreadPool(n))
}

fun WorkRunners.cachedThreadPool(): WorkRunner {
  return from(Executors.newCachedThreadPool())
}

fun WorkRunners.from(service: ExecutorService): WorkRunner {
  return ExecutorServiceWorkRunner(service)
}
