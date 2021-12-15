package kt.mobius

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls

class TestErrorHandler : MobiusHooks.ErrorHandler {

    private val count = atomic(0)
    private val internalHandledErrors = atomicArrayOfNulls<Throwable>(10)

    val handledErrors: List<Throwable>
        get() = List(count.value) { internalHandledErrors[it].value!! }

    override fun handleError(error: Throwable) {
        internalHandledErrors[count.getAndIncrement()].value = error
    }
}