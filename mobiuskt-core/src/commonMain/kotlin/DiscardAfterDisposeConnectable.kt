package kt.mobius

import kt.mobius.DiscardAfterDisposeWrapper.Companion.wrapConnection
import kt.mobius.DiscardAfterDisposeWrapper.Companion.wrapConsumer
import kt.mobius.disposables.CompositeDisposable
import kt.mobius.functions.Consumer

/**
 * A [Connectable] that ensures that [Connection]s created by the wrapped
 * [Connectable] don't emit or receive any values after being disposed.
 *
 * This only acts as a safeguard, you still need to make sure that the
 * Connectable disposes of resources correctly.
 */
public class DiscardAfterDisposeConnectable<I, O>(
        private val actual: Connectable<I, O>
) : Connectable<I, O> {

    override fun connect(output: Consumer<O>): Connection<I> {
        val safeOutput = wrapConsumer(output)
        val input = actual.connect(safeOutput)
        val safeInput = wrapConnection(input)

        val disposable = CompositeDisposable.from(safeInput, safeOutput)

        return object : Connection<I> {
            override fun accept(value: I) {
                safeInput.accept(value)
            }

            override fun dispose() {
                disposable.dispose()
            }
        }
    }
}
