package kt.mobius.android

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import kt.mobius.runners.*
import java.util.*
import java.util.concurrent.*

/**
 * An internal implementation of [LiveQueue] that allows posting values.
 *
 * @param T The type of data to store and queue up
 */
internal class MutableLiveQueue<T>(
    private val effectsWorkRunner: WorkRunner,
    capacity: Int
) : LiveQueue<T> {
    private inner class LifecycleObserverHelper : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            onLifecycleChanged(event)
        }
    }

    private val lock = Any()
    private val pausedEffectsQueue: BlockingQueue<T>

    private var liveObserver: Observer<T>? = null

    private var pausedObserver: Observer<Iterable<T>>? = null
    private var lifecycleOwnerIsPaused = true

    init {
        pausedEffectsQueue = ArrayBlockingQueue(capacity)
    }

    override fun hasActiveObserver(): Boolean {
        return liveObserver != null && !lifecycleOwnerIsPaused
    }

    override fun hasObserver(): Boolean {
        return liveObserver != null
    }

    override fun setObserver(lifecycleOwner: LifecycleOwner, liveEffectsObserver: Observer<T>) {
        setObserver(lifecycleOwner, liveEffectsObserver, null)
    }

    override fun setObserver(
        lifecycleOwner: LifecycleOwner,
        liveEffectsObserver: Observer<T>,
        pausedEffectsObserver: Observer<Iterable<T>>?
    ) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return  // ignore
        }
        synchronized(lock) {
            this.liveObserver = liveEffectsObserver
            this.pausedObserver = pausedEffectsObserver
            lifecycleOwnerIsPaused = true
            lifecycleOwner.lifecycle.addObserver(LifecycleObserverHelper())
        }
    }

    override fun clearObserver() {
        synchronized(lock) {
            liveObserver = null
            pausedObserver = null
            lifecycleOwnerIsPaused = true
            pausedEffectsQueue.clear()
        }
    }

    /**
     * This method will try to send the posted data to any observers
     *
     * @param data The data to send
     */
    fun post(data: T) {
        synchronized(lock) {
            if (lifecycleOwnerIsPaused) {
                check(pausedEffectsQueue.offer(data)) {
                    "Maximum effect queue size (${pausedEffectsQueue.size}) exceeded when posting: $data"
                }
            } else {
                effectsWorkRunner.post(kt.mobius.runners.Runnable { sendToLiveObserver(data) })
            }
        }
    }

    private fun onLifecycleChanged(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> synchronized(lock) {
                lifecycleOwnerIsPaused = false
                sendQueuedEffects()
            }

            Lifecycle.Event.ON_PAUSE -> synchronized(lock) { lifecycleOwnerIsPaused = true }
            Lifecycle.Event.ON_DESTROY -> synchronized(lock) { clearObserver() }
            else -> Unit
        }
    }

    private fun sendQueuedEffects() {
        val queueToSend: Queue<T> = LinkedList()
        synchronized(lock) {
            if (lifecycleOwnerIsPaused || pausedObserver == null || pausedEffectsQueue.isEmpty()) {
                return
            }
            pausedEffectsQueue.drainTo(queueToSend)
        }
        effectsWorkRunner.post(kt.mobius.runners.Runnable { sendToPausedObserver(queueToSend) })
    }

    private fun sendToLiveObserver(data: T) {
        synchronized(lock) {
            liveObserver?.onChanged(data)
        }
    }

    private fun sendToPausedObserver(queuedData: Queue<T>) {
        synchronized(lock) {
            pausedObserver?.onChanged(queuedData)
        }
    }
}
