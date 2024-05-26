package kt.mobius.disposables

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.internal_util.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/** A [Disposable] that disposes of other disposables. */
@JsExport
public class CompositeDisposable private constructor(disposables: Array<out Disposable>) : Disposable {
    private val lock = SynchronizedObject()

    private val disposables = disposables.copyOf()

    public override fun dispose() {
        synchronized(lock) {
            for (disposable in disposables) {
                disposable.dispose()
            }
        }
    }

    public companion object {
        /**
         * Creates a [CompositeDisposable] that holds onto the provided disposables and disposes of
         * all of them once its [dispose] method is called.
         *
         * @param disposables disposables to be disposed of
         * @return a Disposable that mass-disposes of the provided disposables
         */
        @JvmStatic
        @JsName("from")
        public fun from(vararg disposables: Disposable): Disposable = CompositeDisposable(disposables)
    }
}
