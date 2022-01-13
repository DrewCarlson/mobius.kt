package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.functions.Consumer
import kotlin.js.JsName

/**
 * Processes events and emits effects and models as a result of that.
 *
 * @param [M] model type
 * @param [E] event type
 * @param [F] effect descriptor type
 */
internal class EventProcessor<M, E, F> internal constructor(
    private val store: MobiusStore<M, E, F>,
    private val effectConsumer: Consumer<F>,
    private val modelConsumer: Consumer<M>
) {
    private val lock = SynchronizedObject()

    fun update(event: E): Unit = synchronized(lock) {
        val next = store.update(event)

        next.ifHasModel(
            object : Consumer<M> {
                override fun accept(value: M) {
                    dispatchModel(value)
                }
            })
        dispatchEffects(next.effects())
    }

    private fun dispatchModel(model: M) {
        modelConsumer.accept(model)
    }

    private fun dispatchEffects(effects: Iterable<F>) {
        for (effect in effects) {
            effectConsumer.accept(effect)
        }
    }

    /**
     * Factory for event processors.
     *
     * @param [M] model type
     * @param [E] event type
     * @param [F] effect descriptor type
     */
    data class Factory<M, E, F>(val store: MobiusStore<M, E, F>) {

        @JsName("create")
        fun create(effectConsumer: Consumer<F>, modelConsumer: Consumer<M>): EventProcessor<M, E, F> {
            return EventProcessor(store, effectConsumer, modelConsumer)
        }
    }
}
