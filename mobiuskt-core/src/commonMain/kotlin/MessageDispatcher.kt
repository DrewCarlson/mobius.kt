package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner
import kotlin.jvm.Volatile

/**
 * Dispatches messages to a given runner.
 *
 * @param M message type (typically a model, event, or effect descriptor type)
 */
internal class MessageDispatcher<M>(
    val runner: WorkRunner,
    val consumer: Consumer<M>
) : Consumer<M>, Disposable {

    @Volatile
    private var disabled = false

    override fun accept(value: M) {
        runner.post(
            object : Runnable {
                override fun run() {
                    if (disabled) {
                        println("Message ignored because the dispatcher is disabled: $value")
                    } else {
                        try {
                            consumer.accept(value)
                        } catch (throwable: Throwable) {
                            println("Consumer threw an exception when accepting message: $value")
                            println(throwable.message)
                        }
                    }
                }
            })
    }

    override fun dispose() {
        runner.dispose()
    }

    fun disable() {
        disabled = true
    }
}
