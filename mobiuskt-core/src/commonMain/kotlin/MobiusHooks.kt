package kt.mobius

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kt.mobius.internal_util.format
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Allows configuration of how Mobius handles programmer errors through setting a custom [ErrorHandler]
 * via the [setErrorHandler] method. The default handler prints errors with [println].
 */
@JsExport
public object MobiusHooks {
    private val DEFAULT_ERROR_HANDLER = ErrorHandler { error ->
        newLogger("ErrorHandler").error(error, "Uncaught error")
    }

    private val DEFAULT_LOGGER_FACTORY = InternalLoggerFactory(::DefaultInternalLogger)

    private val errorHandler = atomic(DEFAULT_ERROR_HANDLER)
    private val loggerFactory = atomic(DEFAULT_LOGGER_FACTORY)

    internal fun newLogger(tag: String): InternalLogger {
        return loggerFactory.value.create(tag)
    }

    public fun handleError(error: Throwable) {
        errorHandler.value.handleError(error)
    }

    /**
     * Changes the error handler that is used by Mobius for internal errors.
     *
     * @param newHandler the new handler to use.
     */
    public fun setErrorHandler(newHandler: ErrorHandler) {
        errorHandler.update { newHandler }
    }

    /** Sets the error handler to the default one. */
    public fun setDefaultErrorHandler() {
        errorHandler.update { DEFAULT_ERROR_HANDLER }
    }

    /**
     * Set a custom [InternalLoggerFactory] backed by your preferred platform
     * logging APIs.
     *
     * @param newLoggerFactory the new factory to produce tagged [InternalLogger]s.
     */
    public fun setLoggerFactory(newLoggerFactory: InternalLoggerFactory) {
        loggerFactory.update { newLoggerFactory }
    }

    /** Sets the logger factory to produce the default `println` logger. */
    public fun setDefaultInternalLogger() {
        loggerFactory.update { DEFAULT_LOGGER_FACTORY }
    }

    public fun interface ErrorHandler {
        public fun handleError(error: Throwable)
    }

    public fun interface InternalLoggerFactory {
        public fun create(tag: String): InternalLogger
    }

    public interface InternalLogger {
        @JsName("errorWithArgs")
        public fun error(message: String, vararg args: Any?)

        @JsName("errorWithThrowable")
        public fun error(throwable: Throwable, message: String, vararg args: Any?)

        @JsName("warnWithArgs")
        public fun warn(message: String, vararg args: Any?)

        @JsName("warnWithThrowable")
        public fun warn(throwable: Throwable, message: String, vararg args: Any?)

        @JsName("debugWithArgs")
        public fun debug(message: String, vararg args: Any?)

        @JsName("debugWithThrowable")
        public fun debug(throwable: Throwable, message: String, vararg args: Any?)
    }
}

internal class DefaultInternalLogger internal constructor(tag: String) : MobiusHooks.InternalLogger {

    private val tagPrefix = "[$tag] "

    private fun logLine(level: String, message: String, args: Array<out Any?> = emptyArray()) {
        println("{}{}: $message".format(tagPrefix, level, *args))
    }

    override fun error(message: String, vararg args: Any?) {
        logLine("e", message, args)
    }

    override fun error(throwable: Throwable, message: String, vararg args: Any?) {
        logLine("e", message, args)
        logLine("e", throwable.stackTraceToString())
    }

    override fun warn(message: String, vararg args: Any?) {
        logLine("w", message, args)
    }

    override fun warn(throwable: Throwable, message: String, vararg args: Any?) {
        logLine("w", message, args)
        logLine("w", throwable.stackTraceToString())
    }

    override fun debug(message: String, vararg args: Any?) {
        logLine("d", message, args)
    }

    override fun debug(throwable: Throwable, message: String, vararg args: Any?) {
        logLine("d", message, args)
        logLine("d", throwable.stackTraceToString())
    }
}
