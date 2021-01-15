package kt.mobius.functions

/** Interface for simple functions.  */
interface Function<T, R> {

    companion object {
        inline operator fun <T, R> invoke(crossinline function: (T) -> R) =
            object : Function<T, R> {
                override fun apply(value: T): R = function(value)
            }
    }

    @mpp.JsName("apply")
    fun apply(value: T): R
}
