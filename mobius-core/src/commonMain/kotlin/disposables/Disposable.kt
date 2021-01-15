package kt.mobius.disposables

/**
 * A [Disposable] is an object that may be holding on to references or resources that need to
 * be released when the object is no longer needed. The dispose method is invoked to release
 * resources that the object is holding.
 */
interface Disposable {
    companion object {
        inline operator fun invoke(crossinline dispose: () -> Unit): Disposable {
            return object : Disposable {
                override fun dispose() {
                    dispose()
                }
            }
        }
    }

    /**
     * Dispose of all resources associated with this object.
     *
     * The object will no longer be valid after dispose has been called, and any further calls to
     * dispose won't have any effect.
     */
    fun dispose()
}
