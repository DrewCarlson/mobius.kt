package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer


public class EventSourceConnectable<M, E> private constructor(
    private val eventSource: EventSource<E>
) : Connectable<M, E> {

    override fun connect(output: Consumer<E>): Connection<M> {
        val disposable = eventSource.subscribe(output)
        return object : Connection<M> {
            private val lock = SynchronizedObject()

            override fun accept(value: M) {}

            override fun dispose() {
                synchronized(lock) {
                    disposable.dispose()
                }
            }
        }
    }

    public companion object {
        public fun <M, E> create(source: EventSource<E>): Connectable<M, E> {
            return EventSourceConnectable(source)
        }
    }
}
