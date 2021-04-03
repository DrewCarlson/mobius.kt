package kt.mobius.flow

import kt.mobius.runners.WorkRunner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** A [WorkRunner] that launches work on a [CoroutineDispatcher]. */
class DispatcherWorkRunner(
    dispatcher: CoroutineDispatcher
) : WorkRunner {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    override fun post(runnable: Runnable) {
        scope.launch { runnable.run() }
    }

    override fun dispose() {
        scope.cancel()
    }
}
