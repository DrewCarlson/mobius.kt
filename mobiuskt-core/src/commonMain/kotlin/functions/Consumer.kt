package kt.mobius.functions

import kotlin.js.JsName

/** Interface for consuming values. */
public fun interface Consumer<V> {

    @JsName("accept")
    public fun accept(value: V)
}
