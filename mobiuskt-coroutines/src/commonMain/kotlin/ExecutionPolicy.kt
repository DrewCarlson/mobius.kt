package kt.mobius.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest

/**
 * Defines execution behavior of handler functions added to a [subtypeEffectHandler].
 */
public interface ExecutionPolicy {
    public fun <F, E> execute(
        transform: suspend FlowCollector<E>.(effect: F) -> Unit,
        effects: Flow<F>
    ): Flow<E>

    /**
     * Handle Effects one at a time, completing each in order before handling the next.
     */
    public object Sequential : ExecutionPolicy {

        override fun <F, E> execute(
            transform: suspend FlowCollector<E>.(effect: F) -> Unit,
            effects: Flow<F>
        ): Flow<E> {
            return effects.transform(transform)
        }
    }

    /**
     * Handle Effects concurrently up to the provided [limit], defaulting to [DEFAULT_CONCURRENCY].
     *
     * @param limit The number of effects that that can be handled concurrently, defaulting to [DEFAULT_CONCURRENCY].
     */
    public class Concurrent(
        private val limit: Int
    ) : ExecutionPolicy {

        @OptIn(FlowPreview::class)
        public companion object : ExecutionPolicy by Concurrent(DEFAULT_CONCURRENCY)

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun <F, E> execute(
            transform: suspend FlowCollector<E>.(effect: F) -> Unit,
            effects: Flow<F>
        ): Flow<E> {
            return effects.flatMapMerge(concurrency = limit) { effect ->
                flow { transform(effect) }
            }
        }
    }

    /**
     * Handle only the most recent Effect, cancelling the previous operation
     * when a new Effect is dispatched.
     */
    public object Latest : ExecutionPolicy {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun <F, E> execute(
            transform: suspend FlowCollector<E>.(effect: F) -> Unit,
            effects: Flow<F>
        ): Flow<E> {
            return effects.transformLatest(transform)
        }
    }
}
