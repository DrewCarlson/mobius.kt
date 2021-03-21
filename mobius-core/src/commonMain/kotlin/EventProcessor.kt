package kt.mobius

import kt.mobius.functions.Consumer
import kotlin.js.JsName

/**
 * Processes events and emits effects and models as a result of that.
 *
 * @param [M] model type
 * @param [E] event type
 * @param [F] effect descriptor type
 */
class EventProcessor<M, E, F> internal constructor(
    val store: MobiusStore<M, E, F>,
    val effectConsumer: Consumer<F>,
    val modelConsumer: Consumer<M>
) {
    private object LOCK

    // concurrency note: the two below fields are only read and written in synchronized sections,
    // hence no need for further coordination.
    private val eventsReceivedBeforeInit = ArrayList<E>()
    private var initialised = false

    fun init(): Unit = mpp.synchronized(LOCK) {
        if (initialised) {
            throw IllegalStateException("already initialised")
        }

        val first = store.init()

        dispatchModel(first.model())
        dispatchEffects(first.effects())

        initialised = true
        for (event in eventsReceivedBeforeInit) {
            update(event)
        }
    }

    fun update(event: E): Unit = mpp.synchronized(LOCK) {
        if (!initialised) {
            eventsReceivedBeforeInit.add(event)
            return
        }

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
