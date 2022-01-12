package kt.mobius.android

import kt.mobius.*
import kt.mobius.android.runners.MainThreadWorkRunner

public object MobiusAndroid {
    @JvmStatic
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, MainThreadWorkRunner.create())
    }

    @JvmStatic
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        init: Init<M, F>
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, init, MainThreadWorkRunner.create())
    }
}
