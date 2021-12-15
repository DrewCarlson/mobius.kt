package kt.mobius.test

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner

class TestWorkRunner : WorkRunner {

    private val lock = SynchronizedObject()
    private var queue = arrayListOf<Runnable>()

    var isDisposed = false
        private set

    override fun post(runnable: Runnable) {
        synchronized(lock) {
            check(!isDisposed) { "this WorkRunner has already been disposed." }
            queue.add(runnable)
        }
    }

    private fun runOne() {
        lateinit var runnable: Runnable
        synchronized(lock) {
            if (queue.isEmpty()) return
            runnable = queue.removeFirst()
        }
        runnable.run()
    }

    fun runAll() {
        while (true) {
            synchronized(lock) {
                if (queue.isEmpty()) return
            }
            runOne()
        }
    }

    override fun dispose() {
        synchronized(lock) {
            isDisposed = true
            queue.clear()
        }
    }
}
