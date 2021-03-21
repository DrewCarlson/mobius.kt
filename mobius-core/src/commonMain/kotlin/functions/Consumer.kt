package kt.mobius.functions

import kotlin.js.JsName

/** Interface for consuming values. */
fun interface Consumer<V> {

    @JsName("accept")
    fun accept(value: V)
}
