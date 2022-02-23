package kt.mobius.flow

import kotlinx.coroutines.flow.Flow
import kotlin.js.JsExport

@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public fun interface FlowTransformer<I, O> {
    public operator fun invoke(input: Flow<I>): Flow<O>
}

public fun <I, O> flowTransformer(
    transformer: FlowTransformer<I, O>
): FlowTransformer<I, O> = transformer
