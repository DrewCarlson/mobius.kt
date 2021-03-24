package kt.mobius

import kotlin.js.JsName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/** Defines the entry into the initial state of a Mobius loop. */
class First<M, F> internal constructor(
    /** the initial model to use */
    private val model: M,
    /** the possibly empty set of effects to initially dispatch */
    private val effects: Set<F>
) {

    fun model() = model
    fun effects() = effects

    /** Check if this First contains effects  */
    fun hasEffects() = effects.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is First<*, *>) return false
        if (other.model != model) return false
        return other.effects.containsAll(effects)
    }

    override fun hashCode(): Int {
        var result = model?.hashCode() ?: 0
        result = 31 * result + effects.hashCode()
        return result
    }

    companion object {

        /**
         * Create a [First] with the provided model and the optional initial effects.
         *
         * @param model the model to initialize the loop with
         * @param [M] the model type
         * @param [F] the effect type
         */
        @JvmStatic
        @JvmOverloads
        @JsName("first")
        fun <M, F> first(model: M, effects: Set<F> = emptySet()): First<M, F> {
            return First(model, effects)
        }

        /**
         * Create a [First] with the provided model and the optional initial effects.
         *
         * @param model the model to initialize the loop with
         * @param [M] the model type
         * @param [F] the effect type
         */
        @JvmStatic
        fun <M, F> first(model: M, vararg effects: F): First<M, F> {
            return First(model, effects.toSet())
        }
    }
}
