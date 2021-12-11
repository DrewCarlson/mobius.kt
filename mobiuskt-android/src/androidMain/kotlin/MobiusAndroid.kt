package kt.mobius.android

import kt.mobius.Mobius
import kt.mobius.MobiusLoop
import kt.mobius.android.runners.MainThreadWorkRunner

public object MobiusAndroid {
    @JvmStatic
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, MainThreadWorkRunner.create())
    }
}
