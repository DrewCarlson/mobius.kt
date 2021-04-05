package kt.mobius

import kt.mobius.functions.Consumer

class RecordingModelObserver<S> : Consumer<S> {

    private val recorder = RecordingConsumer<S>()

    override fun accept(value: S) {
        recorder.accept(value)
    }

    /*fun waitForChange(timeoutMs: Long): Boolean {
      return recorder.waitForChange(timeoutMs)
    }*/

    fun valueCount(): Int {
        return recorder.valueCount()
    }

    @SafeVarargs
    fun assertStates(vararg expectedStates: S) {
        recorder.assertValues(*expectedStates)
    }
}
