package kt.mobius.runners

/**
 * Interface for posting runnables to be executed on a thread.
 * The runnables must all be executed on the same thread for a given WorkRunner.
 */
actual object WorkRunners {

    actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }
}
