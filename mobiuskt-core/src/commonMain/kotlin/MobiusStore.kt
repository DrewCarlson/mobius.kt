package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import mpp.ensureNeverFrozen
import kotlin.jvm.JvmStatic

/** Responsible for holding and updating the current model. */
public class MobiusStore<M, E, F> internal constructor(
    private val update: Update<M, E, F>,
    startModel: M
) {
    private val lock = SynchronizedObject()

    private var currentModel: M = startModel

    init {
        ensureNeverFrozen()
    }

    public fun update(event: E): Next<M, F> = synchronized(lock) {
        update.update(currentModel, event).also { next ->
            currentModel = next.modelOrElse(currentModel)
        }
    }

    public companion object {
        @JvmStatic
        public fun <M, E, F> create(update: Update<M, E, F>, startModel: M): MobiusStore<M, E, F> {
            return MobiusStore(update, startModel)
        }
    }
}
