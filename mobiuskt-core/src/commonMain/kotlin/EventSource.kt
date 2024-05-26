package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.internal_util.JsExport
import kotlin.js.JsName

/**
 * Interface for event sources.
 *
 * The event source is used for subscribing to events that are external to the Mobius
 * application. This is primarily meant to be used for environmental events - events that come from
 * external signals, like change of network connectivity or a periodic timer, rather than happening
 * because of an effect being triggered or the UI being interacted with.
 *
 * @param [E] the event class
 */
@JsExport
public fun interface EventSource<E> {

    /**
     * Subscribes the supplied consumer to the events from this event source, until the returned
     * [Disposable] is disposed. Multiple such subscriptions can be in place concurrently for a
     * given event source, without affecting each other.
     *
     * @param eventConsumer the consumer that should receive events from the source
     * @return a disposable used to stop the source from emitting any more events to this consumer
     */
    @Suppress("NON_EXPORTABLE_TYPE")
    @JsName("subscribe")
    public fun subscribe(eventConsumer: Consumer<E>): Disposable
}
