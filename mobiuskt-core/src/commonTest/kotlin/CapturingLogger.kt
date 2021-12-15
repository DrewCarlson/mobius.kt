package kt.mobius


internal class CapturingLogger<M, E, F> : MobiusLoop.Logger<M, E, F> {
    val beforeInits = arrayListOf<M>()
    val afterInits = arrayListOf<AfterInitArgs<M, F>>()
    val initErrors = arrayListOf<InitErrorArgs<M>>()
    val beforeUpdates = arrayListOf<BeforeUpdateArgs<M, E>>()
    val afterUpdates = arrayListOf<AfterUpdateArgs<M, E, F>>()
    val updateErrors = arrayListOf<UpdateErrorArgs<M, E>>()
    override fun beforeInit(model: M) {
        beforeInits.add(model)
    }

    override fun afterInit(model: M, result: First<M, F>) {
        afterInits.add(AfterInitArgs(model, result))
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        initErrors.add(InitErrorArgs(model, exception))
    }

    override fun beforeUpdate(model: M, event: E) {
        beforeUpdates.add(BeforeUpdateArgs(model, event))
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        afterUpdates.add(AfterUpdateArgs(model, event, result))
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        updateErrors.add(UpdateErrorArgs(model, event, exception))
    }

    internal data class AfterInitArgs<M, F>(
        val model: M,
        val first: First<M, F>,
    )

    internal data class InitErrorArgs<M>(
        val model: M,
        val exception: Throwable
    )

    internal data class BeforeUpdateArgs<M, E>(
        val model: M,
        val event: E
    )

    internal data class AfterUpdateArgs<M, E, F>(
        val model: M,
        val event: E,
        val next: Next<M, F>
    )

    data class UpdateErrorArgs<M, E>(
        val model: M,
        val event: E,
        val exception: Throwable
    )
}