package kt.mobius.runners

import kt.mobius.functions.Producer

internal actual object DefaultWorkRunners {

    actual fun eventWorkRunnerProducer() = Producer {
        WorkRunners.singleThread()
    }

    actual fun effectWorkRunnerProducer() = Producer {
        WorkRunners.cachedThreadPool()
    }
}
