package kt.mobius.runners


import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

public actual object WorkRunners {

    @JvmStatic
    public actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }

    @JvmStatic
    public fun singleThread(): WorkRunner {
        return from(Executors.newSingleThreadExecutor())
    }

    @JvmStatic
    public fun fixedThreadPool(n: Int): WorkRunner {
        return from(Executors.newFixedThreadPool(n))
    }

    @JvmStatic
    public fun cachedThreadPool(): WorkRunner {
        return from(Executors.newCachedThreadPool())
    }

    @JvmStatic
    public fun from(service: ExecutorService): WorkRunner {
        return ExecutorServiceWorkRunner(service)
    }
}
