package kt.mobius

import kotlinx.atomicfu.atomic
import kotlin.js.JsExport

/**
 * Allows configuration of how Mobius handles programmer errors through setting a custom [ErrorHandler]
 * via the [setErrorHandler] method. The default handler prints errors with [println].
 */
@JsExport
public object MobiusHooks {
    private val DEFAULT_ERROR_HANDLER = ErrorHandler { error ->
        println("Uncaught error: ${error.stackTraceToString()}")
    }

    private val errorHandler = atomic(DEFAULT_ERROR_HANDLER)

    public fun handleError(error: Throwable) {
        errorHandler.value.handleError(error)
    }

    /**
     * Changes the error handler that is used by Mobius for internal errors.
     *
     * @param newHandler the new handler to use.
     */
    public fun setErrorHandler(newHandler: ErrorHandler) {
        errorHandler.value = newHandler
    }

    /** Sets the error handler to the default one. */
    public fun setDefaultErrorHandler() {
        errorHandler.value = DEFAULT_ERROR_HANDLER
    }

    public fun interface ErrorHandler {
        public fun handleError(error: Throwable)
    }
}
