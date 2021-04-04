package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import java.util.ArrayList

internal class FakeEventSource<E> : EventSource<E> {

    private val myConsumers = ArrayList<Consumer<E>>()

    fun emit(toEmit: E) {
        for (myConsumer in myConsumers) {
            myConsumer.accept(toEmit)
        }
    }

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        myConsumers.add(eventConsumer)

        return object : Disposable {
            override fun dispose() {
                // no-op for now; add a 'disposed' flag or something if needed later
            }
        }
    }
}
