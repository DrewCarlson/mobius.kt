package kt.mobius.flow

import kotlinx.coroutines.flow.Flow

fun interface FlowTransformer<I, O> {
    operator fun invoke(input: Flow<I>): Flow<O>
}

fun <I, O> flowTransformer(
    transformer: FlowTransformer<I, O>
): FlowTransformer<I, O> = transformer
