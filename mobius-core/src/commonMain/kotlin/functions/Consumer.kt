package kt.mobius.functions

import kotlin.js.JsName

/** Interface for consuming values. */
interface Consumer<V> {
    companion object {
        inline operator fun <V> invoke(crossinline accept: (value: V) -> Unit): Consumer<V> {
            return object : Consumer<V> {
                override fun accept(value: V) = accept(value)
            }
        }
    }

    @JsName("accept")
    fun accept(value: V)
}

