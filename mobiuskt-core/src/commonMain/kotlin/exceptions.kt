package kt.mobius

import kotlin.jvm.JvmOverloads

/**
 * Indicates that a [Connectable] connection caused an unhandled exception.
 *
 * This is a programmer error - connections must never throw unhandled exceptions, instead the
 * connection is expected to catch the exception and emit a special error value to its output
 * consumer.
 *
 * An example of this would be that if loading an HTTP request throws an exception, then the
 * connection should emit a LoadingDataFailed event to communicate the failure to [Update].
 */
class ConnectionException(
    val effect: Any,
    throwable: Throwable
) : RuntimeException(effect.toString(), throwable) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ConnectionException) return false
        return effect == other.effect
    }

    override fun hashCode(): Int {
        return effect.hashCode()
    }
}

/**
 * Exception to be thrown by a [Connectable] that doesn't support multiple simultaneous
 * connections.
 */
class ConnectionLimitExceededException @JvmOverloads constructor(
    message: String? = null,
    throwable: Throwable? = null
) : RuntimeException(message, throwable)
