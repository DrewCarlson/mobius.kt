package kt.mobius.runners

import kt.mobius.disposables.Disposable

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
public interface WorkRunner : Disposable {
    public fun post(runnable: Runnable)
}
