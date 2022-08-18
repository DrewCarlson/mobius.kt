package kt.mobius.flow

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.runners.WorkRunner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunners

@Suppress("UnusedReceiverParameter")
public fun WorkRunners.fromDispatcher(dispatcher: CoroutineDispatcher): WorkRunner {
    return DispatcherWorkRunner(dispatcher)
}

/** A [WorkRunner] that launches work on a [CoroutineDispatcher]. */
public class DispatcherWorkRunner(
    dispatcher: CoroutineDispatcher
) : WorkRunner {

    private val lock = SynchronizedObject()
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    override fun post(runnable: Runnable) {
        synchronized(lock) {
            scope.launch { runnable.run() }
        }
    }

    override fun dispose() {
        synchronized(lock) {
            scope.cancel()
        }
    }
}
