package kt.mobius.runners

import kt.mobius.functions.Producer

internal actual class DefaultWorkRunners {

    actual fun eventWorkRunnerProducer() =
        Producer<WorkRunner> {
            WorkRunners.immediate()
        }

    actual fun effectWorkRunnerProducer() =
        Producer<WorkRunner> {
            WorkRunners.immediate()
        }
}
