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
    private val runner: WorkRunner,
    private val consumer: Consumer<M>
) : Consumer<M>, Disposable {

    @Volatile
    private var disposed = false

    override fun accept(value: M) {
        if (disposed) return
        runner.post(object : Runnable {
            override fun run() {
                try {
                    consumer.accept(value)
                } catch (throwable: Throwable) {
                    println("Consumer threw an exception when accepting message: $value")
                    println(throwable.message)
                }
            }
        })
    }

    override fun dispose() {
        disposed = true
        runner.dispose()
    }
}
