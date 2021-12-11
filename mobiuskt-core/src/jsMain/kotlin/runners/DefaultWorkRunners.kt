package kt.mobius.runners

import kt.mobius.functions.Producer

private external fun setTimeout(func: () -> Unit, timeout: Long)

internal actual class DefaultWorkRunners {
    actual fun eventWorkRunnerProducer() = Producer<WorkRunner> {
        object : WorkRunner {
            override fun post(runnable: Runnable) {
                setTimeout(runnable::run, 0)
            }

            override fun dispose() {
            }
        }
    }

    actual fun effectWorkRunnerProducer() = Producer<WorkRunner> {
        object : WorkRunner {
            override fun post(runnable: Runnable) {
                setTimeout(runnable::run, 0)
            }

            override fun dispose() {
            }
        }
    }
}
