package kt.mobius.android

import androidx.lifecycle.*
import kt.mobius.*
import kt.mobius.disposables.*
import kt.mobius.functions.*
import java.util.concurrent.*

/** An extension of MutableLiveData that allows its Active/Inactive state to be observed.  */
internal class ObservableMutableLiveData<T> : MutableLiveData<T>(), EventSource<Boolean> {

    private val stateListeners: MutableList<Consumer<Boolean>> = CopyOnWriteArrayList()

    private fun notifyListeners(value: Boolean) {
        for (listener in stateListeners) {
            listener.accept(value)
        }
    }

    override fun onActive() {
        super.onActive()
        notifyListeners(true)
    }

    override fun onInactive() {
        super.onInactive()
        notifyListeners(false)
    }

    override fun subscribe(eventConsumer: Consumer<Boolean>): Disposable {
        stateListeners.add(eventConsumer)
        return Disposable { stateListeners.remove(eventConsumer) }
    }
}
