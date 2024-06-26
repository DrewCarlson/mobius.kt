package kt.mobius

import kt.mobius.internal_util.JsExport
import kotlin.js.JsName

/**
 * An interface representing the Init function used by Mobius for starting or resuming execution of
 * a program from a given model.
 *
 * Implementations of this interface must be pure - they should have no model and no
 * side-effects. This means that given the same arguments, the function must always return the same
 * [First].
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public fun interface Init<M, F> {

    @JsName("init")
    public fun init(model: M): First<M, F>
}
