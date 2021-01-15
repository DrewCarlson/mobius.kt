package kt.mobius

import kt.mobius.functions.Consumer

class ControllerStateRunning<M, E, F>(
    private val actions: ControllerActions<M, E>,
    private val renderer: Connection<M>,
    loopFactory: MobiusLoop.Factory<M, E, F>,
    private val startModel: M
) : ControllerStateBase<M, E>() {

    private val loop = loopFactory.startFrom(startModel)

    override val stateName: String
        get() = "running"

    override val isRunning: Boolean
        get() = true

    fun start() {
        loop.observe(
            Consumer { model ->
                actions.postUpdateView(model)
            })
    }

    override fun onDispatchEvent(event: E) {
        loop.dispatchEvent(event)
    }

    override fun onUpdateView(model: M) {
        renderer.accept(model)
    }

    override fun onStop() {
        loop.dispose()
        val mostRecentModel = loop.mostRecentModel
        actions.goToStateCreated(renderer, mostRecentModel)
    }

    override fun onGetModel(): M {
        val model = loop.mostRecentModel
        return model ?: startModel
    }
}
