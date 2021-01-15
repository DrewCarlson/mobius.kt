package kt.mobius.test

import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner
import java.util.*

class TestWorkRunner : WorkRunner {

    private val queue = LinkedList<Runnable>()

    var isDisposed = false
        private set

    override fun post(runnable: Runnable) {
        synchronized(queue) {
            if (isDisposed) {
                throw IllegalStateException("this WorkRunner has already been disposed.")
            }
            queue.add(runnable)
        }
    }

    private fun runOne() {
        lateinit var runnable: Runnable
        synchronized(queue) {
            if (queue.isEmpty()) return
            runnable = queue.remove()
        }
        runnable.run()
    }

    fun runAll() {
        while (true) {
            synchronized(queue) {
                if (queue.isEmpty()) return
            }
            runOne()
        }
    }

    override fun dispose() {
        synchronized(queue) {
            isDisposed = true
            queue.clear()
        }
    }
}
