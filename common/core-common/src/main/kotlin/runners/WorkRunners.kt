package com.spotify.mobius.runners


/**
 * Interface for posting runnables to be executed on a thread.
 * The runnables must all be executed on the same thread for a given WorkRunner.
 */
expect object WorkRunners {

  fun immediate(): WorkRunner
}
