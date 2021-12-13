package kt.mobius.runners

import platform.darwin.*

@Suppress("unused")
public fun WorkRunners.fromDispatchQueue(dispatchQueue: dispatch_queue_t): WorkRunner {
    return DispatchQueueWorkRunner(dispatchQueue)
}

public class DispatchQueueWorkRunner(
    private val dispatchQueue: dispatch_queue_t
) : WorkRunner {
    override fun post(runnable: Runnable) {
        dispatch_async(dispatchQueue, runnable::run)
    }

    override fun dispose(): Unit = Unit
}
