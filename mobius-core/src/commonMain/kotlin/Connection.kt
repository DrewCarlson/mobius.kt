package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer

/**
 * Handle for a connection created by [Connectable].
 *
 *
 * Used for sending values to the connection and to dispose of it and all resources associated
 * with it.
 */
interface Connection<I> : Disposable, Consumer<I> {

    /**
     * Send a value this connection. Implementations may receive values from different threads and are
     * thus expected to be thread-safe.
     *
     * @param value the value that should be sent to the connection
     */
    override fun accept(value: I)

    /**
     * Disconnect this connection and dispose of all resources associated with it.
     *
     *
     * The connection will no longer be valid after dispose has been called. No further values will
     * be accepted, and any repeated calls to dispose should be ignored.
     */
    override fun dispose()
}
