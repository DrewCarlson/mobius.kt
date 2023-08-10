package kt.mobius.extras.patterns

import kt.mobius.Next
import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update
import kt.mobius.functions.BiFunction
import kt.mobius.functions.Function
import kotlin.jvm.JvmStatic

/**
 * Helper class for putting an update function inside another update function.
 *
 * It is sometimes useful to compose two update functions that each have their own model, events,
 * and effects. Typically, when you do this you will store the inner model inside the outer model,
 * and route some outer events to the inner update function. This class helps you wire up this
 * conversion between inner and outer model, events, and effects.
 *
 * The outer update function must still make the decision if the inner update function should be
 * called or not, this class only helps with converting the types of the inner update function
 *
 * @param [M] the outer model type
 * @param [E] the outer event type
 * @param [F] the outer effect type
 * @param [MI] the inner model type
 * @param [EI] the inner event type
 * @param [FI] the inner effect type
 */
public class InnerUpdate<M, E, F, MI, EI, FI>(
    public val innerUpdate: Update<MI, EI, FI>,
    public val modelExtractor: Function<M, MI>,
    public val eventExtractor: Function<E, EI?>,
    public val modelUpdater: BiFunction<M, MI, M>,
    public val innerEffectHandler: InnerEffectHandler<M, F, FI>
) : Update<M, E, F> {

    public companion object {
        public class Builder<M, E, F, MI, EI, FI> {
            private lateinit var _innerUpdate: Update<MI, EI, FI>
            private lateinit var _modelExtractor: Function<M, MI>
            private lateinit var _eventExtractor: Function<E, EI?>
            private lateinit var _modelUpdater: BiFunction<M, MI, M>
            private lateinit var _innerEffectHandler: InnerEffectHandler<M, F, FI>

            public fun innerUpdate(innerUpdate: Update<MI, EI, FI>): Builder<M, E, F, MI, EI, FI> =
                apply { _innerUpdate = innerUpdate }

            public fun modelExtractor(modelExtractor: Function<M, MI>): Builder<M, E, F, MI, EI, FI> =
                apply { _modelExtractor = modelExtractor }

            public fun eventExtractor(eventExtractor: Function<E, EI?>): Builder<M, E, F, MI, EI, FI> =
                apply { _eventExtractor = eventExtractor }

            public fun modelUpdater(modelUpdater: BiFunction<M, MI, M>): Builder<M, E, F, MI, EI, FI> =
                apply { _modelUpdater = modelUpdater }

            public fun innerEffectHandler(
                innerEffectHandler: InnerEffectHandler<M, F, FI>
            ): Builder<M, E, F, MI, EI, FI> =
                apply { _innerEffectHandler = innerEffectHandler }

            public fun build(): InnerUpdate<M, E, F, MI, EI, FI> = InnerUpdate(
                innerUpdate = if (::_innerUpdate.isInitialized) _innerUpdate else {
                    error("You must call innerUpdate()")
                },
                modelExtractor = if (::_modelExtractor.isInitialized) _modelExtractor else {
                    error("You must call modelExtractor()")
                },
                eventExtractor = if (::_eventExtractor.isInitialized) _eventExtractor else {
                    error("You must call eventExtractor()")
                },
                modelUpdater = if (::_modelUpdater.isInitialized) _modelUpdater else {
                    error("You must call modelUpdater()")
                },
                innerEffectHandler = if (::_innerEffectHandler.isInitialized) _innerEffectHandler else {
                    error("You must call innerEffectHandler()")
                }
            )
        }

        @JvmStatic
        public fun <M, E, F, MI, EI, FI> builder(): Builder<M, E, F, MI, EI, FI> = Builder()
    }

    override fun update(model: M, event: E): Next<M, F> {
        val innerModel = modelExtractor.apply(model)
        val innerEvent = checkNotNull(eventExtractor.apply(event)) {
            val className = event?.run { this::class.simpleName }.orEmpty()
            "InnerUpdate cannot handle event '$className' because the `eventExtractor` returned null"
        }

        val innerNext = innerUpdate.update(innerModel, innerEvent)

        val modelUpdated = innerNext.hasModel()

        val newModel = if (modelUpdated) {
            modelUpdater.apply(model, innerNext.modelUnsafe())
        } else {
            model
        }

        return innerEffectHandler.handleInnerEffects(newModel, modelUpdated, innerNext.effects())
    }
}

/**
 * Interface for handling effects from an inner update function when using [InnerUpdate].
 *
 * @param [M] the outer model type
 * @param [F] the outer effect type
 * @param [FI] the inner effect type
 */
public interface InnerEffectHandler<M, F, FI> {

    public companion object {
        public inline operator fun <M, F, FI> invoke(
            crossinline handler: (
                model: M,
                modelUpdated: Boolean,
                innerEffects: Set<FI>
            ) -> Next<M, F>
        ): InnerEffectHandler<M, F, FI> = object : InnerEffectHandler<M, F, FI> {
            override fun handleInnerEffects(model: M, modelUpdated: Boolean, innerEffects: Set<FI>): Next<M, F> {
                return handler(model, modelUpdated, innerEffects)
            }
        }
    }

    /**
     * Handle effects emitted from an inner update function.
     *
     *
     * The outer model has already been updated when this method is called, and the arguments let
     * you know if the model was updated or not. When handling effects you may further modify the
     * model, emit new outer effects, or even choose to ignore the updated outer model.
     *
     * @param model the updated outer model
     * @param modelUpdated true if the outer model was updated
     * @param innerEffects the effects emitted by the inner update function
     */
    public fun handleInnerEffects(model: M, modelUpdated: Boolean, innerEffects: Set<FI>): Next<M, F>
}

public object InnerEffectHandlers {

    /**
     * Create an inner effect handler that ignores inner effects.
     *
     * The resulting next will be an [Next.next] or a [Next.noChange]
     * depending on if the outer model changed.
     */
    public fun <M, F, FI> ignoreEffects(): InnerEffectHandler<M, F, FI> {
        return InnerEffectHandler { model, modelUpdated, _ ->
            if (modelUpdated) next(model) else noChange()
        }
    }

    /**
     * Create an inner effect handler that maps inner effects.
     *
     * This can be used for example to wrap an inner effect in an outer effect, or to map inner
     * effects to outer effects.
     *
     * If there are no inner effects, then the resulting next will be an [Next.next]
     * or a [Next.noChange] depending on if the outer model changed.
     */
    public fun <M, F, FI> mapEffects(f: Function<FI, F>): InnerEffectHandler<M, F, FI> {
        return InnerEffectHandler { model, modelUpdated, innerEffects ->
            if (innerEffects.isEmpty()) {
                if (modelUpdated) next(model)
                else noChange()
            } else {
                val effects = innerEffects.map(f::apply).toSet()
                if (modelUpdated) next(model, effects)
                else dispatch(effects)
            }
        }
    }
}
