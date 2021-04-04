package kt.mobius.extras

import kt.mobius.First
import kt.mobius.MobiusLoop.Logger
import kt.mobius.Next
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * A [Logger] that delegates all logging to a list of provided loggers. Useful if you have
 * different types of loggers that you would like to us simultaneously while maintaining single
 * responsibility per logger implementation
 *
 * @param M The loop's Model type
 * @param E The loop's Event type
 * @param F The loop's Effect type
 */
class CompositeLogger<M, E, F> private constructor(
    private val loggers: List<Logger<M, E, F>>
) : Logger<M, E, F> {

    override fun beforeInit(model: M) {
        loggers.forEach { it.beforeInit(model) }
    }

    override fun afterInit(model: M, result: First<M, F>) {
        loggers.forEach { it.afterInit(model, result) }
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        loggers.forEach { it.exceptionDuringInit(model, exception) }
    }

    override fun beforeUpdate(model: M, event: E) {
        loggers.forEach { it.beforeUpdate(model, event) }
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        loggers.forEach { it.afterUpdate(model, event, result) }
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        loggers.forEach { it.exceptionDuringUpdate(model, event, exception) }
    }

    companion object {

        @JvmStatic
        @JsName("from")
        fun <M, E, F> from(vararg loggers: Logger<M, E, F>): Logger<M, E, F> {
            return CompositeLogger(loggers.toList())
        }
    }
}
