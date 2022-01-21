package kt.mobius.android

import androidx.lifecycle.*

/**
 * An interface for an object emitter which emits objects exactly once. This can be used to send
 * effects that need to be handled only once, while also providing a mechanism to queue and handle
 * effects that occur while the lifecycle-owner is in a paused state.
 *
 * @param T The type of object to store
 */
public interface LiveQueue<T> {
    /**
     * Returns `true` if the current observer is in a Resumed state
     * `false` if the current observer is not Resumed, or there is no current observer
     */
    public fun hasActiveObserver(): Boolean

    /**
     * Returns `true` if there is an observer of this [LiveQueue]
     * `false` if there is no current observer assigned
     */
    public fun hasObserver(): Boolean

    /**
     * A utility method for calling .setObserver] that substitutes null for the optional observer.
     * See linked method doc for full info.
     */
    public fun setObserver(lifecycleOwner: LifecycleOwner, liveEffectsObserver: Observer<T>)

    /**
     * The [LiveQueue] supports only a single observer, so calling this method will
     * override any previous observers set.
     *
     * Effects while the lifecycle is active are sent only to the liveEffectsObserver.
     *
     * Once the lifecycle owner goes into Paused state, no effects will be forwarded, however, if the
     * state changes to Resumed, all effects that occurred while Paused will be passed to the optional
     * pausedEffectsObserver. If this optional observer is not provided, these effects will be
     * ignored.
     *
     * Effects that occur while there is no lifecycle owner set will not be queued.
     *
     * @param lifecycleOwner This required parameter is used to queue effects while its state is
     * Paused and to resume sending effects once it resumes.
     * @param liveEffectsObserver This required observer will be forwarded all effects while the
     * lifecycle owner is in a Resumed state.
     * @param pausedEffectsObserver The nullable observer will be invoked when the lifecycle owner
     * resumes, and will receive a queue of effects, ordered as they occurred while paused.
     */
    public fun setObserver(
        lifecycleOwner: LifecycleOwner,
        liveEffectsObserver: Observer<T>,
        pausedEffectsObserver: Observer<Iterable<T>>?
    )

    /**
     * Removes the current observer and clears any queued effects.
     *
     * To replace the observer without clearing queued effects, use [setObserver]
     */
    public fun clearObserver()
}
