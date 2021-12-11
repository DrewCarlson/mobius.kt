package kt.mobius.runners

import kotlin.native.concurrent.Worker

public class NativeWorkRunner(
    private val worker: Worker
) : WorkRunner {

    init {
        check(Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            "Using NativeWorkRunner requires the experimental memory model.\nSee https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md"
        }
    }

    override fun post(runnable: Runnable) {
        worker.executeAfter(operation = runnable::run)
    }

    override fun dispose() {
        worker.requestTermination(processScheduledJobs = false)
    }
}