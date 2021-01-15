package kt.mobius.functions

/** Interface for simple functions with two arguments.  */
interface BiFunction<T, U, R> {

    companion object {
        inline operator fun <T, U, R> invoke(
            crossinline function: (value1: T, value2: U) -> R
        ) = object : BiFunction<T, U, R> {
            override fun apply(value1: T, value2: U): R = function(value1, value2)
        }
    }

    fun apply(value1: T, value2: U): R
}
