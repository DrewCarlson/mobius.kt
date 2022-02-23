package kt.mobius.disposables

import kotlin.js.JsExport

/**
 * A [Disposable] is an object that may be holding on to references or resources that need to
 * be released when the object is no longer needed. The dispose method is invoked to release
 * resources that the object is holding.
 */
@JsExport
public fun interface Disposable {

    /**
     * Dispose of all resources associated with this object.
     *
     * The object will no longer be valid after dispose has been called, and any further calls to
     * dispose won't have any effect.
     */
    public fun dispose()
}
