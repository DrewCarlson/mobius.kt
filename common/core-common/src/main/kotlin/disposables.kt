package com.spotify.mobius.disposables


/**
 * A [Disposable] is an object that may be holding on to references or resources that need to
 * be released when the object is no longer needed. The dispose method is invoked to release
 * resources that the object is holding.
 */
interface Disposable {
  companion object {
    operator fun invoke(dispose: () -> Unit): Disposable {
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

/** A [Disposable] that disposes of other disposables. */
class CompositeDisposable private constructor(disposables: Array<out Disposable>) : Disposable {
  private object LOCK

  private val disposables = disposables.copyOf()

  override fun dispose() = synchronized(LOCK) {
    for (disposable in disposables) {
      disposable.dispose()
    }
  }

  companion object {
    /**
     * Creates a [CompositeDisposable] that holds onto the provided disposables and disposes of
     * all of them once its [dispose] method is called.
     *
     * @param disposables disposables to be disposed of
     * @return a Disposable that mass-disposes of the provided disposables
     */
    operator fun invoke(vararg disposables: Disposable) = CompositeDisposable(disposables)
  }
}
