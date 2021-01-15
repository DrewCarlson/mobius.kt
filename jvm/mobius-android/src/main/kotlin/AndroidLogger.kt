package kt.mobius.android

import android.util.Log
import kt.mobius.First
import kt.mobius.MobiusLoop
import kt.mobius.Next

class AndroidLogger<M, E, F>(
    private val tag: String
) : MobiusLoop.Logger<M, E, F> {

    companion object {
        @JvmStatic
        fun <M, E, F> tag(tag: String): AndroidLogger<M, E, F> {
            return AndroidLogger(tag)
        }
    }

    override fun beforeInit(model: M) {
        Log.d(tag, "Initializing loop")
    }

    override fun afterInit(model: M, result: First<M, F>) {
        Log.d(tag, "Loop initialized, starting from model: " + result.model())
        result.effects().forEach { effect ->
            Log.d(tag, "Effect dispatched: $effect")
        }
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        Log.e(tag, "FATAL ERROR: exception during initialization from model '$model'", exception)
    }

    override fun beforeUpdate(model: M, event: E) {
        Log.d(tag, "Event received: $event")
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        if (result.hasModel()) {
            Log.d(tag, "Model updated: " + result.modelUnsafe())
        }

        result.effects().forEach { effect ->
            Log.d(tag, "Effect dispatched: $effect")
        }
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        Log.e(tag, "FATAL ERROR: exception updating model '$model' with event '$event'", exception)
    }
}
