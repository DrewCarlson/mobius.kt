package kt.mobius.test

import kt.mobius.functions.Consumer

public class RecordingModelObserver<S> : Consumer<S> {

    private val recorder = RecordingConsumer<S>()

    override fun accept(value: S) {
        recorder.accept(value)
    }

    /*fun waitForChange(timeoutMs: Long): Boolean {
      return recorder.waitForChange(timeoutMs)
    }*/

    public fun valueCount(): Int {
        return recorder.valueCount()
    }

    public fun assertStates(vararg expectedStates: S) {
        recorder.assertValues(*expectedStates)
    }
}