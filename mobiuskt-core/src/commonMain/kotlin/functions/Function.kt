package kt.mobius.functions

import kotlin.js.JsName

/** Interface for simple functions.  */
public fun interface Function<T, R> {

    @JsName("apply")
    public fun apply(value: T): R
}
