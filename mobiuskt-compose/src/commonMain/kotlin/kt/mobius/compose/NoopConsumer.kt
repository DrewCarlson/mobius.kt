package kt.mobius.compose

import kt.mobius.functions.Consumer

internal class NoopConsumer<V> : Consumer<V> {
    override fun accept(value: V) {
    }
}