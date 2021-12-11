package kt.mobius.android.runners

import android.os.Looper
import kt.mobius.runners.WorkRunner

/** A [LooperWorkRunner] that executes runnables on Android's main thread.  */
public class MainThreadWorkRunner private constructor() : LooperWorkRunner(Looper.getMainLooper()) {
    public companion object {
        /** Creates a [WorkRunner] that runs work on Android's main thread.  */
        @JvmStatic
        public fun create(): MainThreadWorkRunner {
            return MainThreadWorkRunner()
        }
    }
}
