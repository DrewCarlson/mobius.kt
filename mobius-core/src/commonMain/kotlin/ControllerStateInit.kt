package kt.mobius

class ControllerStateInit<M, E>(
    private val actions: ControllerActions<M, E>,
    private var nextModelToStartFrom: M
) : ControllerStateBase<M, E>() {

    override val stateName: String
        get() = "init"

    override fun onConnect(view: Connectable<M, E>) {
        actions.goToStateCreated(view, nextModelToStartFrom)
    }

    override fun onReplaceModel(model: M) {
        nextModelToStartFrom = model
    }

    override fun onGetModel(): M {
        return nextModelToStartFrom
    }
}
