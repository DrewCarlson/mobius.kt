package kt.mobius.android.runners

import android.os.Handler
import android.os.Looper
import kt.mobius.runners.WorkRunner

/** A work runner that uses a [Looper] to run work.  */
open class LooperWorkRunner internal constructor(looper: Looper) : WorkRunner {
    private val handler: Handler = Handler(looper)

    @Volatile
    private var disposed: Boolean = false

    /** Will cancel all Runnables posted to this looper.  */
    override fun dispose() {
        handler.removeCallbacksAndMessages(null)
        disposed = true
    }

    /**
     * Will post the provided runnable to the looper for processing.
     *
     * @param runnable the runnable you would like to execute
     */
    override fun post(runnable: kt.mobius.runners.Runnable) {
        if (disposed) return
        handler.post(runnable)
    }

    companion object {

        /**
         * Creates a [WorkRunner] backed by the provided [Looper]
         *
         * @param looper the looper to use for processing work
         * @return a [WorkRunner] that uses the provided [Looper] for processing work
         */
        @JvmStatic
        fun using(looper: Looper): LooperWorkRunner {
            return LooperWorkRunner(looper)
        }
    }
}
