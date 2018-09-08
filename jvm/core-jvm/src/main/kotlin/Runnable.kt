package com.spotify.mobius.runners

actual interface Runnable : java.lang.Runnable {
  actual override fun run()
}
