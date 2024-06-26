package kt.mobius.flow

import kt.mobius.Mobius
import kt.mobius.MobiusLoop
import kt.mobius.Update
import kt.mobius.internal_util.JsExport

@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public object FlowMobius {

    public fun <M, E, F> loopFrom(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        startModel: M
    ): FlowTransformer<E, M> =
        FlowMobiusLoop(loopFactory, startModel)

    public fun <M, E, F> loop(
        update: Update<M, E, F>,
        effectHandler: FlowTransformer<F, E>
    ): MobiusLoop.Builder<M, E, F> {
        return Mobius.loop(update, effectHandler.asConnectable())
    }
}
