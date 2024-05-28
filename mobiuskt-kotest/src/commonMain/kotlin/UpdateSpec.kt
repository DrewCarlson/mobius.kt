package kt.mobius.kotest

import kt.mobius.Next
import kt.mobius.Next.Companion.next
import kt.mobius.Update


public class UpdateSpec<M, E, F> internal constructor(
    internal val startModel: M,
    internal val update: Update<M, E, F>,
)

public fun <M, E, F> Update<M, E, F>.given(given: M): UpdateSpec<M, E, F> {
    return UpdateSpec(given, this)
}

public fun <M, E, F> UpdateSpec<M, E, F>.whenEvent(vararg event: E): Next<M, F> {
    var currentModel = startModel
    var next = next<M, F>(startModel)
    for (e in event) {
        next = update.update(currentModel, e)

        if (next.hasModel()) {
            currentModel = next.modelUnsafe()
        }
    }
    return next
}