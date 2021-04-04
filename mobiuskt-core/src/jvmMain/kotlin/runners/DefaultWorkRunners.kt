package kt.mobius.runners

import kt.mobius.functions.Producer
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

actual class DefaultWorkRunners {
    private val threadFactory = MyThreadFactory()

    actual fun eventWorkRunnerProducer() = Producer {
        WorkRunners.from(Executors.newSingleThreadExecutor(threadFactory))
    }

    actual fun effectWorkRunnerProducer() = Producer {
        WorkRunners.from(Executors.newCachedThreadPool(threadFactory))
    }

    private class MyThreadFactory : ThreadFactory {
        private val threadCount = AtomicLong(0)

        override fun newThread(r: java.lang.Runnable?): Thread {
            return Executors.defaultThreadFactory().newThread(r).apply {
                name = "mobius-thread-${threadCount.incrementAndGet()}"
            }
        }
    }
}
