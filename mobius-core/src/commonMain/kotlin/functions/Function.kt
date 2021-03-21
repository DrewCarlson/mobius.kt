package kt.mobius.functions

import kotlin.js.JsName

/** Interface for simple functions.  */
fun interface Function<T, R> {

    @JsName("apply")
    fun apply(value: T): R
}
