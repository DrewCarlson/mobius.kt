package com.spotify.mobius.disposables

import synchronized2


/** A [Disposable] that disposes of other disposables. */
class CompositeDisposable private constructor(disposables: Array<out Disposable>) : Disposable {
  private object LOCK

  private val disposables = disposables.copyOf()

  override fun dispose() = synchronized2(LOCK) {
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
    fun from(vararg disposables: Disposable) = CompositeDisposable(disposables)
  }
}
