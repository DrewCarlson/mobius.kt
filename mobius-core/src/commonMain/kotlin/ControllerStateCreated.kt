package kt.mobius

class ControllerStateCreated<M, E, F>(
    private val actions: ControllerActions<M, E>,
    private val renderer: Connection<M>,
    private var nextModelToStartFrom: M
) : ControllerStateBase<M, E>() {

    override val stateName: String
        get() = "created"

    override fun onDisconnect() {
        renderer.dispose()
        actions.goToStateInit(nextModelToStartFrom)
    }

    override fun onStart() {
        actions.goToStateRunning(renderer, nextModelToStartFrom)
    }

    override fun onReplaceModel(model: M) {
        nextModelToStartFrom = model
    }

    override fun onGetModel(): M {
        return nextModelToStartFrom
    }
}
