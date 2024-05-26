package kt.mobius.runners

import kt.mobius.functions.Producer

internal actual object DefaultWorkRunners {
    actual fun eventWorkRunnerProducer() = Producer { WorkRunners.immediate() }

    actual fun effectWorkRunnerProducer() = Producer { WorkRunners.immediate() }
}
