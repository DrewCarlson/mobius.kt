package kt.mobius

import kt.mobius.functions.Consumer
import kotlin.js.JsName

/**
 * API for something that can be connected to be part of a [MobiusLoop].
 *
 *
 * Primarily used in [Mobius.loop] to define the effect handler of a
 * Mobius loop. In that case, the incoming values will be effects, and the outgoing values will be
 * events that should be sent back to the loop.
 *
 *
 * Alternatively used in [Controller.connect] to connect a view to the
 * controller. In that case, the incoming values will be models, and the outgoing values will be
 * events.
 *
 * @param I the incoming value type
 * @param O the outgoing value type
 */
fun interface Connectable<I, O> {

    /**
     * Create a new connection that accepts input values and sends outgoing values to a supplied
     * consumer.
     *
     *
     * Must return a new [Connection] that accepts incoming values. After [Connection.dispose]
     * is called on the returned [Connection], the connection must be broken, and no more values
     * may be sent to the output consumer.
     *
     *
     * Every call to this method should create a new independent connection that can be disposed of
     * individually without affecting the other connections. If your Connectable doesn't support this,
     * it should throw a [ConnectionLimitExceededException] if someone tries to connect a second
     * time before disposing of the first connection.
     *
     * @param output the consumer that the new connection should use to emit values
     * @return a new connection
     * @throws ConnectionLimitExceededException should be thrown if there are too many concurrent
     * connections to this Connectable; this should be caused by incorrect usage of the
     * Connectable, and is considered an irrecoverable error
     */
    @JsName("connect")
    fun connect(output: Consumer<O>): Connection<I>
}
