package kt.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

public actual object WorkRunners {

    private val threadFactory = MyThreadFactory()

    @JvmStatic
    public actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }

    @JvmStatic
    public fun singleThread(): WorkRunner {
        return from(Executors.newSingleThreadExecutor(threadFactory))
    }

    @JvmStatic
    public fun fixedThreadPool(n: Int): WorkRunner {
        return from(Executors.newFixedThreadPool(n, threadFactory))
    }

    @JvmStatic
    public fun cachedThreadPool(): WorkRunner {
        return from(Executors.newCachedThreadPool(threadFactory))
    }

    @JvmStatic
    public fun from(service: ExecutorService): WorkRunner {
        return ExecutorServiceWorkRunner(service)
    }

    private class MyThreadFactory : ThreadFactory {
        private val threadCount = AtomicLong(0)

        override fun newThread(r: java.lang.Runnable): Thread {
            return Executors.defaultThreadFactory().newThread(r).apply {
                name = "mobius-thread-${threadCount.incrementAndGet()}"
            }
        }
    }
}
