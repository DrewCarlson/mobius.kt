package com.spotify.mobius.runners


actual object WorkRunners {

  actual fun immediate(): WorkRunner {
    return ImmediateWorkRunner()
  }
}
