package kt.mobius.runners

import kotlin.native.concurrent.Worker

public actual object WorkRunners {

    public actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }

    public fun nativeWorker(
        name: String? = null,
        errorReporting: Boolean = true
    ): WorkRunner {
        return NativeWorkRunner(Worker.start(errorReporting, name))
    }

    public fun nativeWorker(worker: Worker): WorkRunner {
        return NativeWorkRunner(worker)
    }
}
