package kt.mobius.functions

import kt.mobius.internal_util.JsExport

/** Interface for simple functions with two arguments.  */
@JsExport
public fun interface BiFunction<T, U, R> {

    @Suppress("NON_EXPORTABLE_TYPE")
    public fun apply(value1: T, value2: U): R
}
