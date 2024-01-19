package kt.mobius.darwin

import kt.mobius.*
import kt.mobius.runners.*

public object MobiusDarwin {
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, DispatchQueueWorkRunner.main())
    }

    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        init: Init<M, F>
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, init, DispatchQueueWorkRunner.main())
    }
}
