package kt.mobius.disposables

import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/** A [Disposable] that disposes of other disposables. */
class CompositeDisposable private constructor(disposables: Array<out Disposable>) : Disposable {
    private object LOCK

    private val disposables = disposables.copyOf()

    override fun dispose() = mpp.synchronized(LOCK) {
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
        @JvmStatic
        @JsName("from")
        fun from(vararg disposables: Disposable): Disposable = CompositeDisposable(disposables)
    }
}
