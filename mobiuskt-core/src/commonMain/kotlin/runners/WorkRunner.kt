package kt.mobius.runners

import kt.mobius.disposables.Disposable
import kt.mobius.internal_util.JsExport

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
@JsExport
public interface WorkRunner : Disposable {

    /**
     * Must discard any new [Runnable] immediately after dispose method of [Disposable] is
     * called. Not doing this may result in undesired side effects, crashes, race conditions etc.
     */
    @Suppress("NON_EXPORTABLE_TYPE")
    public fun post(runnable: Runnable)
}
