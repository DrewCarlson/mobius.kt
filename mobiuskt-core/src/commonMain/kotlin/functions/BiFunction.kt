package kt.mobius.functions

/** Interface for simple functions with two arguments.  */
public fun interface BiFunction<T, U, R> {

    public fun apply(value1: T, value2: U): R
}
