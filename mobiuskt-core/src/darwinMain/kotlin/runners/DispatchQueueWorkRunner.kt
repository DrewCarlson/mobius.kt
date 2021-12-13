package kt.mobius.runners

import platform.darwin.*

@Suppress("unused")
public fun WorkRunners.fromDispatchQueue(dispatchQueue: dispatch_queue_t): WorkRunner {
    return DispatchQueueWorkRunner(dispatchQueue)
}

public class DispatchQueueWorkRunner(
    private val dispatchQueue: dispatch_queue_t
) : WorkRunner {

    init {
        check(Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            "Using DispatchQueueWorkRunner requires the experimental memory model.\nSee https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md"
        }
    }

    override fun post(runnable: Runnable) {
        dispatch_async(dispatchQueue, runnable::run)
    }

    override fun dispose(): Unit = Unit
}
