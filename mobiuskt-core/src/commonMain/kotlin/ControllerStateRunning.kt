package kt.mobius

internal class ControllerStateRunning<M, E, F>(
    private val actions: ControllerActions<M, E>,
    private val renderer: Connection<M>,
    loopFactory: MobiusLoop.Factory<M, E, F>,
    modelToStartFrom: M,
    init: Init<M, F>?,
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

    fun start() {
        loop.observe(actions::postUpdateView)
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
