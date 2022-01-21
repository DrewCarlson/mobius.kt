package kt.mobius.android

import kt.mobius.*
import kt.mobius.functions.*

/**
 * Interface used by the MobiusLoopViewModel to pass all dependencies necessary
 * to create a MobiusLoop.Factory.
 */
public fun interface MobiusLoopFactoryProvider<M, E, F, V> {
    /**
     * Creates a MobiusLoop Factory given all the possible dependencies from the MobiusLoopViewModel
     *
     * @param viewEffectConsumer The consumer of View Effects that can be used in your Effect Handler
     * @param activeModelEventSource An Event Source that emits the current active/inactive state of
     * the ViewModel, based on whether the ViewModel's [MobiusLoopViewModel.model] has any active
     * observers or not. This can be used in conjuncture with `ToggledEventSource` to be used to control
     * the emissions of any other given Event Source.
     * @return The factory used to create the loop
     */
    public fun create(
        viewEffectConsumer: Consumer<V>,
        activeModelEventSource: EventSource<Boolean>
    ): MobiusLoop.Factory<M, E, F>
}
