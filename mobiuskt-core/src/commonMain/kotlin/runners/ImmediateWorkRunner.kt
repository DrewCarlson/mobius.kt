package kt.mobius.runners

import kotlinx.atomicfu.locks.SynchronizedObject
import mpp.synchronized

/**
 * A [WorkRunner] that immediately invokes the [Runnable] you post on the thread you
 * posted from.
 */
class ImmediateWorkRunner : WorkRunner {
    private val lock = object : SynchronizedObject() {}

    private var disposed: Boolean = false

    override fun post(runnable: Runnable): Unit =
        synchronized(lock) {
            if (disposed) return

            runnable.run()
        }

    override fun dispose(): Unit =
        synchronized(lock) {
            disposed = true
        }
}
