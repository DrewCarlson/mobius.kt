package kt.mobius.android.runners

import android.os.Looper
import kt.mobius.runners.WorkRunner

/** A [LooperWorkRunner] that executes runnables on Android's main thread.  */
class MainThreadWorkRunner private constructor() : LooperWorkRunner(Looper.getMainLooper()) {
    companion object {
        /** Creates a [WorkRunner] that runs work on Android's main thread.  */
        @JvmStatic
        fun create(): MainThreadWorkRunner {
            return MainThreadWorkRunner()
        }
    }
}
