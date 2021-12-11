package kt.mobius.functions

/** Interface for producing values. */
public fun interface Producer<V> {
    public fun get(): V
}
