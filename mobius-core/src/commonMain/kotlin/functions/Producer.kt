package kt.mobius.functions

/** Interface for producing values. */
fun interface Producer<V> {
    fun get(): V
}
