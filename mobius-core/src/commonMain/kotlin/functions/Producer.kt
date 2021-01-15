package kt.mobius.functions

/** Interface for producing values. */
interface Producer<V> {
    companion object {
        inline operator fun <V> invoke(crossinline get: () -> V): Producer<V> {
            return object : Producer<V> {
                override fun get() = get()
            }
        }
    }

    fun get(): V
}
