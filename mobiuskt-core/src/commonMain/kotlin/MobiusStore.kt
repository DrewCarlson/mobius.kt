package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import mpp.ensureNeverFrozen
import mpp.synchronized
import kotlin.jvm.JvmStatic

/** Responsible for holding and updating the current model. */
public class MobiusStore<M, E, F> internal constructor(
    private val init: Init<M, F>,
    private val update: Update<M, E, F>,
    startModel: M
) {
    private val lock = object : SynchronizedObject() {}

    private var currentModel: M = startModel

    init {
        ensureNeverFrozen()
    }

    public fun init(): First<M, F> = synchronized(lock) {
        val first = init.init(currentModel!!)
        currentModel = first.model()
        return first
    }

    public fun update(event: E): Next<M, F> = synchronized(lock) {
        val next = update.update(currentModel, event)
        currentModel = next.modelOrElse(currentModel)
        return next
    }

    public companion object {
        @JvmStatic
        public fun <M, E, F> create(init: Init<M, F>, update: Update<M, E, F>, startModel: M): MobiusStore<M, E, F> {
            return MobiusStore(init, update, startModel)
        }
    }
}
