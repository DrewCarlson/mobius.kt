package kt.mobius.test

import kt.mobius.Connection

public interface SimpleConnection<F> : Connection<F> {

    public companion object {
        public inline operator fun <F> invoke(crossinline consumer: (F) -> Unit): SimpleConnection<F> {
            return object : SimpleConnection<F> {
                override fun accept(value: F) {
                    consumer(value)
                }
            }
        }
    }

    override fun dispose() {
        // ignored.
    }
}
