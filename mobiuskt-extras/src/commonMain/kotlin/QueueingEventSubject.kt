package kt.mobius.extras

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.EventSource
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.internal_util.JsExport

/**
 * An EventSource that can also consume events. If it has a subscriber, events will be immediately
 * forwarded to that subscriber. If it doesn't have a subscriber, it will queue up events (up to the
 * maximum capacity specified in the constructor), and forward all queued events to the next
 * subscriber. Only a single subscription at a time is permitted.
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class QueueingEventSubject<E>(
    private val capacity: Int
) : SynchronizedObject(), EventSource<E>, Consumer<E> {
    private enum class State {
        NO_SUBSCRIBER, SUBSCRIBED
    }

    private val queue = ArrayList<E>(capacity)

    // State and subscriber are accessed only in synchronized sections
    private var state = State.NO_SUBSCRIBER
    private var subscriber: Consumer<E>? = null

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        lateinit var queued: List<E>

        // Do not invoke consumer in synchronized section
        synchronized(this) {
            if (state == State.SUBSCRIBED) {
                error("Only a single subscription is supported, previous subscriber is: $subscriber")
            }
            state = State.SUBSCRIBED
            subscriber = eventConsumer
            queued = ArrayList(queue)
            queue.clear()
        }
        queued.forEach(eventConsumer::accept)
        return Unsubscriber()
    }

    override fun accept(value: E) {
        var consumerToInvoke: Consumer<E>? = null

        // Do not invoke consumer in synchronized section
        synchronized(this) {
            when (state) {
                State.NO_SUBSCRIBER -> {
                    check(queue.size < capacity) {
                        "Queue capacity exceeded, cannot queue $value"
                    }
                    queue.add(value)
                }
                State.SUBSCRIBED -> consumerToInvoke = subscriber
            }
        }
        consumerToInvoke?.accept(value)
    }

    private fun unsubscribe() {
        synchronized(this) {
            state = State.NO_SUBSCRIBER
            subscriber = null
        }
    }

    private inner class Unsubscriber : SynchronizedObject(), Disposable {
        private var disposed = false

        override fun dispose() {
            synchronized(this) {
                if (disposed) {
                    return
                }
                disposed = true
                unsubscribe()
            }
        }
    }
}
