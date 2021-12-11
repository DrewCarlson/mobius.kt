package kt.mobius

public interface ControllerActions<M, E> {

    public fun postUpdateView(model: M)

    public fun goToStateInit(nextModelToStartFrom: M)

    public fun goToStateCreated(renderer: Connection<M>, nextModelToStartFrom: M?)

    public fun goToStateCreated(view: Connectable<M, E>, nextModelToStartFrom: M)

    public fun goToStateRunning(renderer: Connection<M>, nextModelToStartFrom: M)
}
