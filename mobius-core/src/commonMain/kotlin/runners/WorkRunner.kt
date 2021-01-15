package kt.mobius.runners

import kt.mobius.disposables.Disposable

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
interface WorkRunner : Disposable {
    companion object {
        inline operator fun invoke(crossinline post: (Runnable) -> Unit): WorkRunner {
            return object : WorkRunner {
                override fun post(runnable: Runnable) {
                    post(runnable)
                }

                override fun dispose() {
                }
            }
        }
    }

    fun post(runnable: Runnable)
}
