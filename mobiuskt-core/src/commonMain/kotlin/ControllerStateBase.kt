package kt.mobius

/**
 * Note: synchronization has to be provided externally, states do not protect themselves from issues
 * related state switching. Use ControllerActions to interact with MobiusControllerActions for any
 * asynchronous action and never call one on-method from another directly.
 */
internal abstract class ControllerStateBase<M, E> {

    private val logger = MobiusHooks.newLogger("ControllerStateBase")
    protected abstract val stateName: String

    open val isRunning: Boolean
        get() = false

    open fun onConnect(view: Connectable<M, E>) {
        error("cannot call connect when in the $stateName state")
    }

    open fun onDisconnect() {
        error("cannot call disconnect when in the $stateName state")
    }

    open fun onStart() {
        error("cannot call start when in the $stateName state")
    }

    open fun onStop() {
        error("cannot call stop when in the $stateName state")
    }

    open fun onReplaceModel(model: M) {
        error("cannot call replaceModel when in the $stateName state")
    }

    abstract fun onGetModel(): M

    open fun onDispatchEvent(event: E) {
        logger.debug(
            "Dropping event that was dispatched when the program was in the '{}' state: '{}'",
            stateName,
            event
        )
    }

    open fun onUpdateView(model: M) {
        logger.debug(
            "Dropping model that was dispatched when the program was in the '{}' state: '{}'",
            stateName,
            model
        )
    }
}
