package kt.mobius.runners

import kotlin.native.concurrent.Worker
import kotlinx.atomicfu.atomic
import kt.mobius.functions.Producer

internal actual object DefaultWorkRunners {

    private val workerCount = atomic(0L)

    private fun createWorker(): Worker {
        val newCount = workerCount.incrementAndGet()
        return Worker.start(name = "mobius-worker-$newCount")
    }

    actual fun eventWorkRunnerProducer(): Producer<WorkRunner> {
        return if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            Producer { WorkRunners.nativeWorker(createWorker()) }
        } else {
            Producer(WorkRunners::immediate)
        }
    }

    actual fun effectWorkRunnerProducer(): Producer<WorkRunner> {
        return if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            Producer { WorkRunners.nativeWorker(createWorker()) }
        } else {
            Producer(WorkRunners::immediate)
        }
    }
}
