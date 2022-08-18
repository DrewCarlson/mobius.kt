package kt.mobius.runners

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.native.concurrent.Worker

public class NativeWorkRunner(
    private val worker: Worker
) : WorkRunner {

    private val lock = SynchronizedObject()

    init {
        check(Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            "Using NativeWorkRunner requires the experimental memory model.\nSee https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md"
        }
    }

    override fun post(runnable: Runnable) {
        synchronized(lock) {
            worker.executeAfter(operation = runnable::run)
        }
    }

    override fun dispose() {
        synchronized(lock) {
            worker.requestTermination(processScheduledJobs = false)
        }
    }
}
