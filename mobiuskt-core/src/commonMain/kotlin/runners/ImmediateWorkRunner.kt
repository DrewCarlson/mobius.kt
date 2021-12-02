package kt.mobius.runners

import mpp.synchronized

/**
 * A [WorkRunner] that immediately invokes the [Runnable] you post on the thread you
 * posted from.
 */
class ImmediateWorkRunner : WorkRunner {
    private object LOCK

    private var disposed: Boolean = false

    override fun post(runnable: Runnable): Unit =
        synchronized(LOCK) {
            if (disposed) return

            runnable.run()
        }

    override fun dispose(): Unit =
        synchronized(LOCK) {
            disposed = true
        }
}
