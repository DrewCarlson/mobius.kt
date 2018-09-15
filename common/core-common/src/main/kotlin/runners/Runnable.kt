package com.spotify.mobius.runners


expect interface Runnable {
  fun run()
}

operator fun Runnable.invoke(run: () -> Unit): Runnable {
  return object : Runnable {
    override fun run() {
      run()
    }
  }
}
