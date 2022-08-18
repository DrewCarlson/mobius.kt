package kt.mobius.runners

import kotlinx.atomicfu.locks.withLock
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/** A [WorkRunner] implementation that is backed by an [ExecutorService]. */
public class ExecutorServiceWorkRunner(private val service: ExecutorService) : WorkRunner {

    private val lock = ReentrantLock()

    override fun post(runnable: Runnable) {
        lock.withLock {
            if (!service.isTerminated && !service.isShutdown) {
                service.submit(runnable)
            }
        }
    }

    override fun dispose() {
        try {
            lock.withLock {
                val runnables = service.shutdownNow()

                if (runnables.isNotEmpty()) {
                    println("Disposing ExecutorServiceWorkRunner with ${runnables.size} outstanding tasks.")
                }
            }

            if (!service.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                println("ExecutorService shutdown timed out; there are still tasks executing")
            }
        } catch (e: InterruptedException) {
            println("Timeout when disposing work runner")
            println(e)
        }
    }
}
