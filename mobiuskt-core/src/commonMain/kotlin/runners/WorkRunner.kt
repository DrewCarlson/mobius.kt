package kt.mobius.runners

import kt.mobius.disposables.Disposable
import kotlin.js.JsExport

/** Interface for posting runnables to be executed using different scheduling mechanisms. */
@JsExport
public interface WorkRunner : Disposable {

    @Suppress("NON_EXPORTABLE_TYPE")
    public fun post(runnable: Runnable)
}
