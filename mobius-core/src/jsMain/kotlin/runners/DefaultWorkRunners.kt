package kt.mobius.runners

import kt.mobius.functions.Producer
import kotlin.browser.window

actual class DefaultWorkRunners {
    actual fun eventWorkRunnerProducer() = Producer<WorkRunner> {
        object : WorkRunner {
            override fun post(runnable: Runnable) {
                window.setTimeout(runnable::run, 0)
            }

            override fun dispose() {
            }
        }
    }

    actual fun effectWorkRunnerProducer() = Producer<WorkRunner> {
        object : WorkRunner {
            override fun post(runnable: Runnable) {
                window.setTimeout(runnable::run, 0)
            }

            override fun dispose() {
            }
        }
    }
}
