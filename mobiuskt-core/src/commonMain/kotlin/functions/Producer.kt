package kt.mobius.functions

import kotlin.js.JsExport

/** Interface for producing values. */
@JsExport
public fun interface Producer<V> {
    @Suppress("NON_EXPORTABLE_TYPE")
    public fun get(): V
}
