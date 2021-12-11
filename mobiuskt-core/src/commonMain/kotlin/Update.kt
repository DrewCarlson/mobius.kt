package kt.mobius

import kotlin.js.JsName

/**
 * An interface representing the Update function used by Mobius for performing model transitions and
 * requesting side-effects.
 *
 *
 * Implementations of this interface must be pure - they should have no model and no
 * side-effects. This means that given the same arguments, the function must always return the same
 * Next. Each time a new event occurs, the update method will be called and a Next is expected.
 */
public fun interface Update<M, E, F> {

    @JsName("update")
    public fun update(model: M, event: E): Next<M, F>

    public operator fun invoke(model: M, event: E): Next<M, F> {
        return update(model, event)
    }
}
