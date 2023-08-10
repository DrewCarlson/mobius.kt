package kt.mobius.runners

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import platform.darwin.*

@Suppress("UnusedReceiverParameter")
public fun WorkRunners.fromDispatchQueue(dispatchQueue: dispatch_queue_t): WorkRunner {
    return DispatchQueueWorkRunner(dispatchQueue)
}

@Suppress("UnusedReceiverParameter")
public fun WorkRunners.mainDispatchQueue(): WorkRunner {
    return DispatchQueueWorkRunner.main()
}

@Suppress("UnusedReceiverParameter")
public fun WorkRunners.globalDispatchQueue(): WorkRunner {
    return DispatchQueueWorkRunner.global()
}

public class DispatchQueueWorkRunner(
    private val dispatchQueue: dispatch_queue_t
) : WorkRunner {

    private val lock = ReentrantLock()

    override fun post(runnable: Runnable) {
        lock.withLock {
            dispatch_async(dispatchQueue, runnable::run)
        }
    }

    override fun dispose(): Unit = Unit

    public companion object {
        public fun main(): WorkRunner {
            return DispatchQueueWorkRunner(dispatch_get_main_queue())
        }

        public fun global(): WorkRunner {
            return DispatchQueueWorkRunner(dispatch_queue_global_t())
        }
    }
}
