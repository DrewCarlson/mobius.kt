package kt.mobius.functions

import kotlin.js.JsExport

/** Interface for simple functions with two arguments.  */
@JsExport
public fun interface BiFunction<T, U, R> {

    @Suppress("NON_EXPORTABLE_TYPE")
    public fun apply(value1: T, value2: U): R
}
