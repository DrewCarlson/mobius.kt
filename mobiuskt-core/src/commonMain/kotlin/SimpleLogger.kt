package kt.mobius

import kotlin.js.JsExport

/**
 * A basic [MobiusLoop.Logger] implementation backed by the current [MobiusHooks.InternalLogger].
 *
 * Unless you've set a new [MobiusHooks.InternalLogger] with [MobiusHooks.setLoggerFactory], this
 * will be backed by a `println` logger implementation.
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class SimpleLogger<M, E, F>(tag: String) : MobiusLoop.Logger<M, E, F> {
    private val logger = MobiusHooks.newLogger(tag)

    override fun beforeInit(model: M) {
        logger.debug("Initializing loop")
    }

    override fun afterInit(model: M, result: First<M, F>) {
        logger.debug("Loop initialized, starting from model: ${result.model()}")
        result.effects().forEach { logger.debug("Effect Dispatched: {}", it) }
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        logger.error(exception, "FATAL ERROR: exception during initialization from model '{}'", model)
    }

    override fun beforeUpdate(model: M, event: E) {
        logger.debug("Event received: {}", event)
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        if (result.hasModel()) {
            logger.debug("Model updated: {}", result.model())
        }

        result.effects().forEach {
            logger.debug("Effect dispatched: {}", it)
        }
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        logger.error(exception, "FATAL ERROR: exception updating model '{}' with event '{}'", model, event)
    }
}
