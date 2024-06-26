package kt.mobius.functions

import kt.mobius.internal_util.JsExport
import kotlin.js.JsName

/** Interface for consuming values. */
@JsExport
public fun interface Consumer<V> {

    @Suppress("NON_EXPORTABLE_TYPE")
    @JsName("accept")
    public fun accept(value: V)
}
