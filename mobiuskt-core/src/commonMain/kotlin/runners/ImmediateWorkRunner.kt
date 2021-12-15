package kt.mobius.runners

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * A [WorkRunner] that immediately invokes the [Runnable] you post on the thread you
 * posted from.
 */
public class ImmediateWorkRunner : WorkRunner {
    private val lock = SynchronizedObject()

    private var disposed: Boolean = false

    public override fun post(runnable: Runnable): Unit =
        synchronized(lock) {
            if (disposed) return

            runnable.run()
        }

    public override fun dispose(): Unit =
        synchronized(lock) {
            disposed = true
        }
}
