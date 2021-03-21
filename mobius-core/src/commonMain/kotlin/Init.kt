package kt.mobius

import kotlin.js.JsName

/**
 * An interface representing the Init function used by Mobius for starting or resuming execution of
 * a program from a given model.
 *
 *
 * Implementations of this interface must be pure - they should have no model and no
 * side-effects. This means that given the same arguments, the function must always return the same
 * [First].
 */
fun interface Init<M, F> {

    @JsName("init")
    fun init(model: M): First<M, F>

    operator fun invoke(model: M): First<M, F> {
        return init(model)
    }
}
