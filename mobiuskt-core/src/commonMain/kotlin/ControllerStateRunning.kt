package kt.mobius

import kt.mobius.functions.Consumer

public class ControllerStateRunning<M, E, F>(
    private val actions: ControllerActions<M, E>,
    private val renderer: Connection<M>,
    loopFactory: MobiusLoop.Factory<M, E, F>,
    modelToStartFrom: M,
    private val init: Init<M, F>?,
) : ControllerStateBase<M, E>() {

    private val startModel: M
    private val loop: MobiusLoop<M, E, F>

    init {
        if (init == null) {
            loop = loopFactory.startFrom(modelToStartFrom)
            startModel = modelToStartFrom
        } else {
            val first = init.init(modelToStartFrom)
            loop = loopFactory.startFrom(first.model(), first.effects())
            startModel = first.model()
        }
    }

    override val stateName: String
        get() = "running"

    override val isRunning: Boolean
        get() = true

    public fun start() {
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
