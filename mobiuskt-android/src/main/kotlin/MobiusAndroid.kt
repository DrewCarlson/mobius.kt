package kt.mobius.android

import kt.mobius.Mobius
import kt.mobius.MobiusLoop
import kt.mobius.android.runners.MainThreadWorkRunner

object MobiusAndroid {
    @JvmStatic
    fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M
    ): MobiusLoop.Controller<M, E> {
        return Mobius.controller(loopFactory, defaultModel, MainThreadWorkRunner.create())
    }
}
