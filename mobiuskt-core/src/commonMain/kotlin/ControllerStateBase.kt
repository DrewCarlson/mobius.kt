package kt.mobius

/**
 * Note: synchronization has to be provided externally, states do not protect themselves from issues
 * related state switching. Use ControllerActions to interact with MobiusControllerActions for any
 * asynchronous action and never call one on-method from another directly.
 */
abstract class ControllerStateBase<M, E> {

    protected abstract val stateName: String

    open val isRunning: Boolean
        get() = false

    open fun onConnect(view: Connectable<M, E>) {
        throw IllegalStateException("cannot call connect when in the $stateName state")
    }

    open fun onDisconnect() {
        throw IllegalStateException("cannot call disconnect when in the $stateName state")
    }

    open fun onStart() {
        throw IllegalStateException("cannot call start when in the $stateName state")
    }

    open fun onStop() {
        throw IllegalStateException("cannot call stop when in the $stateName state")
    }

    open fun onReplaceModel(model: M) {
        throw IllegalStateException("cannot call replaceModel when in the $stateName state")
    }

    abstract fun onGetModel(): M

    open fun onDispatchEvent(event: E) {
        println("Dropping event that was dispatched when the program was in the '$stateName' state: '$event'")
    }

    open fun onUpdateView(model: M) {
        println("Dropping model that was dispatched when the program was in the '$stateName' state: '$model'")
    }
}
