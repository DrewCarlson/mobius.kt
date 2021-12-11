package kt.mobius.runners

import kt.mobius.disposables.Disposable

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
public interface WorkRunner : Disposable {
    public companion object {
        public inline operator fun invoke(crossinline post: (Runnable) -> Unit): WorkRunner {
            return object : WorkRunner {
                override fun post(runnable: Runnable) {
                    post(runnable)
                }

                override fun dispose() {
                }
            }
        }
    }

    public fun post(runnable: Runnable)
}
