package kt.mobius.flow

import kotlinx.coroutines.flow.Flow

typealias FlowTransformer<I, O> = (input: Flow<I>) -> Flow<O>

fun <I, O> flowTransformer(
    transformer: FlowTransformer<I, O>
): FlowTransformer<I, O> = transformer
