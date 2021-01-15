package kt.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

actual object WorkRunners {

    @JvmStatic
    actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }

    @JvmStatic
    fun singleThread(): WorkRunner {
        return from(Executors.newSingleThreadExecutor())
    }

    @JvmStatic
    fun fixedThreadPool(n: Int): WorkRunner {
        return from(Executors.newFixedThreadPool(n))
    }

    @JvmStatic
    fun cachedThreadPool(): WorkRunner {
        return from(Executors.newCachedThreadPool())
    }

    @JvmStatic
    fun from(service: ExecutorService): WorkRunner {
        return ExecutorServiceWorkRunner(service)
    }
}
