package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer
import kotlin.concurrent.Volatile


public class FireAtLeastOnceObserver<V>(
    private var delegate: Consumer<V>
) : Consumer<V> {
    private val lock = SynchronizedObject()
    private val queueLock = SynchronizedObject()

    private enum class State {
        UNFIRED,
        FIRING,
        READY,
    }

    @Volatile
    private var state = State.UNFIRED
    private val queue: MutableList<V> = mutableListOf<V>()

    override fun accept(value: V) {
        // this is a bit racy, with three threads handling values with order 1, 2 and 3, respectively:
        // 1. thread 1 has called accceptIfUnfired and is in safeConsume, having published its value to
        //    observers, and having just set the state to READY
        // 2. thread 2 has called accept, and is in safeConsume, before the first synchronized section
        // 3. thread 3 has called accept and is about to check the current state.
        //
        // now, if thread 3 reads READY and calls the delegate's accept method directly, before
        // thread 2 sets the state to FIRING and publishes its data, the observer will see 1, 3, 2.
        // this means that this class isn't safe for racing calls to accept(), but given that it's
        // only intended to be used within the event processing, which is sequential, that is not a
        // risk.
        // do note that this class isn't generally useful outside the specific context of event
        // processing.
        if (state != State.READY) {
            safeConsume(value, true)
        } else {
            delegate.accept(value)
        }
    }

    public fun acceptIfFirst(value: V) {
        if (state == State.UNFIRED) {
            safeConsume(value, false)
        }
    }

    private fun safeConsume(value: V, acceptAlways: Boolean) {
        // this synchronized section mustn't call unsafe external code like the delegate's accept
        // method to avoid the risk of deadlocks. It's synchronized because it's changing two stateful
        // fields: the 'state' and the 'queue', and those need to go together to guarantee ordering
        // of the emitted values.
        synchronized(lock) {
            // add this item to the queue if we haven't fired, or if it should be added anyway
            if (state == State.UNFIRED || acceptAlways) {
                queue.add(value)
            }

            // set state to FIRING to prevent acceptIfUnfired from adding items to the queue and messing
            // ordering up - regular accept mustn't invoke the delegate consumer directly until we've
            // processed the queue and entered READY state.
            state = State.FIRING
        }

        synchronized(queueLock) {
            while (queue.isNotEmpty()) {
                val toSend = queue.removeAt(0)
                delegate.accept(toSend)
            }
        }

        synchronized(lock) {
            // it's possible for a racing 'accept' call to add an item to the queue after the last poll
            // above, so check in an exclusive way that the queue is in fact empty
            if (queue.isEmpty()) {
                state = State.READY
            }
        }
    }
}