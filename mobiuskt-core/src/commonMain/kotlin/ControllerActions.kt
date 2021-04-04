package kt.mobius

interface ControllerActions<M, E> {

    fun postUpdateView(model: M)

    fun goToStateInit(nextModelToStartFrom: M)

    fun goToStateCreated(renderer: Connection<M>, nextModelToStartFrom: M?)

    fun goToStateCreated(view: Connectable<M, E>, nextModelToStartFrom: M)

    fun goToStateRunning(renderer: Connection<M>, nextModelToStartFrom: M)
}
