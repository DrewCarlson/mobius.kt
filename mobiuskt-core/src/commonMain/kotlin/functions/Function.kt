package kt.mobius.functions

import kt.mobius.internal_util.JsExport
import kotlin.js.JsName

/** Interface for simple functions.  */
@JsExport
public fun interface Function<T, R> {

    @Suppress("NON_EXPORTABLE_TYPE")
    @JsName("apply")
    public fun apply(value: T): R
}
