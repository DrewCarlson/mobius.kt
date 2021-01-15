package kt.mobius

class SimpleLogger<M, E, F>(private val tag: String) : MobiusLoop.Logger<M, E, F> {
    override fun beforeInit(model: M) {
        println("[$tag] Initializing loop")
    }

    override fun afterInit(model: M, result: First<M, F>) {
        println("[$tag] Loop initialized, starting from model: ${result.model()}")
        result.effects().forEach { println("[$tag] Effect Dispatched: $it") }
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        println("[$tag] FATAL ERROR: exception during initialization from model '$model'")
        println("[$tag] ${exception.message}")
    }

    override fun beforeUpdate(model: M, event: E) {
        println("[$tag] Event received: $event")
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        if (result.hasModel()) {
            println("[$tag] Model updated: ${result.model()}")
        }

        result.effects().forEach {
            println("[$tag] Effect dispatched: $it")
        }
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        println("[$tag] FATAL ERROR: exception updating model '$model' with event '$event'")
        println("[$tag] ${exception.message}")
    }
}
