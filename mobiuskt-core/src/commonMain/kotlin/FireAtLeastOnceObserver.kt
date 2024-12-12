package kt.mobius

import kotlinx.atomicfu.atomic
import kt.mobius.functions.Consumer
import kotlin.concurrent.Volatile


internal class FireAtLeastOnceObserver<V>(
    private var delegate: Consumer<V>
) : Consumer<V> {
    @Volatile
    private var hasStartedEmitting = false
    private val queue = mutableListOf<V>()
    private val firstValue = atomic<WrappedValue<V>?>(null)
    private val processing = atomic(false)

    private class WrappedValue<V>(val value: V) {
        override fun equals(other: Any?): Boolean {
            return other is WrappedValue<*> && other.value == value
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    override fun accept(value: V) {
        queue.add(value)
        drainQueue()
    }

    public fun acceptIfFirst(value: V) {
        // Wrap the value, so that we are able to represent having a `null` value
        // vs. not having a value at all.
        val wrappedValue = WrappedValue(value)
        if (firstValue.compareAndSet(null, wrappedValue)) {
            drainQueue()
        }
    }

    private fun drainQueue() {
        if (!processing.compareAndSet(expect = false, update = true)) {
            // already draining queue
            return
        }

        // We are now in a safe section that can only execute on one thread at the time.
        // If this is the first time, try to emit a value that only can be emitted if it is first.
        if (!hasStartedEmitting) {
            hasStartedEmitting = true
            val wrappedValue = firstValue.value
            if (wrappedValue != null) {
                delegate.accept(wrappedValue.value)
            }
        }

        var done = false

        while (!done) {
            try {
                var toSend: V? = queue.removeFirstOrNull()
                while (toSend != null) {
                    delegate.accept(toSend)
                    toSend = queue.removeFirstOrNull()
                }
            } finally {
                processing.value = false // leave the safe section

                // If the queue is empty or if we can't reacquire the processing lock, we're done,
                // because either there is nothing to do, or someone else will process the queue.
                // Note: it's important that we check the queue first, otherwise we might leak the lock.
                done = queue.isEmpty() || !processing.compareAndSet(expect = false, update = true)
            }
        }
    }
}