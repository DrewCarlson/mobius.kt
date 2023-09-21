package kt.mobius.android

import androidx.lifecycle.*
import kt.mobius.*
import kt.mobius.android.MobiusLoopViewModel.Companion.create
import kt.mobius.android.runners.*
import kt.mobius.runners.*
import java.util.concurrent.atomic.*

/**
 * A Mobius Loop lifecycle handler which is based on the Android ViewModel.
 *
 * This view model has the concept of a View Effect (parameter [V]) which is a type of effect that
 * requires the corresponding Android lifecycle owner to be in an active state i.e. between onResume
 * and onPause. To allow the normal effect handler to send these, the view model will provide a
 * Consumer of these View Effects to the Loop Factory Provider, which can then be passed into the
 * normal Effect handler, so it can delegate view effects where necessary.
 *
 * Since it's based on Android View model, this view model will keep the loop alive as long as
 * the lifecycle owner it is associated with (via a factory to produce it) is not destroyed -
 * meaning the Mobius loop will persist through rotations and brief app minimization to background.
 *
 * While the loop is running but the view is paused, which is between onPause and onDestroy, the
 * view model will keep the latest model/state sent by the loop and will keep a queue of View
 * Effects that have been sent by the effect handler. The loop is automatically disposed when the
 * lifecycle owner is destroyed. To avoid leaks, the maximum number of view effects that are kept
 * when paused is capped - see [create]. Exceeding the limit
 * leads to an [IllegalStateException] when posting further effects.
 *
 * This class is `public` with a `protected` constructor in order to facilitate using
 * it as a key in a [androidx.lifecycle.ViewModelProvider]. It's not intended to be subclassed
 * in order to change its behaviour, and for that reason, all its methods are private or final.
 *
 * @param M The Model with which the Mobius Loop will run
 * @param E The Event type accepted by the loop
 * @param F The Effect type handled by the loop
 * @param V The View Effect which will be emitted by this view model
 */
public open class MobiusLoopViewModel<M, E, F, V> protected constructor(
    loopFactoryProvider: MobiusLoopFactoryProvider<M, E, F, V>,
    modelToStartFrom: M,
    init: Init<M, F>?,
    mainLoopWorkRunner: WorkRunner,
    maxEffectQueueSize: Int
) : ViewModel() {
    private val modelData: ObservableMutableLiveData<M> = ObservableMutableLiveData()
    private val viewEffectQueue: MutableLiveQueue<V>
    private val loop: MobiusLoop<M, E, F>
    private val startModel: M
    private val loopActive = AtomicBoolean(true)

    init {
        viewEffectQueue = MutableLiveQueue(mainLoopWorkRunner, maxEffectQueueSize)
        val loopFactory = loopFactoryProvider.create({ viewEffect: V -> acceptViewEffect(viewEffect) }, modelData)
        val actualInit = if (init == null) null else {
            if (loopFactory is Mobius.Builder) {
                LoggingInit(init, loopFactory.logger)
            } else {
                init
            }
        }
        val first = actualInit?.init(modelToStartFrom)
        val initModel = first?.model() ?: modelToStartFrom
        loop = loopFactory.startFrom(initModel, first?.effects().orEmpty())
        startModel = initModel
        loop.observe { model: M -> onModelChanged(model) }
    }

    public val model: M
        get() {
            val model: M? = loop.mostRecentModel
            return model ?: startModel
        }

    public val models: LiveData<M>
        get() = modelData

    public val viewEffects: LiveQueue<V>
        get() = viewEffectQueue

    public fun dispatchEvent(event: E) {
        if (loopActive.get()) {
            loop.dispatchEvent(event)
        }
    }

    public final override fun onCleared() {
        super.onCleared()
        onClearedInternal()
        loopActive.set(false)
        loop.dispose()
    }

    /**
     * Override this function instead of onCleared, since that is marked final to ensure some
     * operations always happen.
     *
     * This function will be called from onCleared, right before the loop is disposed.
     */
    protected open fun onClearedInternal() {
        /* noop */
    }

    private fun onModelChanged(model: M) {
        modelData.postValue(model)
    }

    private fun acceptViewEffect(viewEffect: V) {
        viewEffectQueue.post(viewEffect)
    }

    public companion object {

        /**
         * Creates a new MobiusLoopViewModel instance.
         *
         * @param loopFactoryProvider The provider for the factory, that gets passed all dependencies
         * @param modelToStartFrom the initial model for the loop
         * @param init the [Init] function of the loop
         * @param maxEffectsToQueue the maximum number of effects to queue while paused, default 100
         * @param M the model type
         * @param E the event type
         * @param F the effect type
         * @param V the view effect type
         */
        @JvmStatic
        @JvmOverloads
        public fun <M, E, F, V> create(
            loopFactoryProvider: MobiusLoopFactoryProvider<M, E, F, V>,
            modelToStartFrom: M,
            init: Init<M, F>? = null,
            maxEffectsToQueue: Int = 100
        ): MobiusLoopViewModel<M, E, F, V> {
            return MobiusLoopViewModel(
                loopFactoryProvider,
                modelToStartFrom,
                init,
                MainThreadWorkRunner.create(),
                maxEffectsToQueue
            )
        }

        internal fun <M, E, F, V> create(
            loopFactoryProvider: MobiusLoopFactoryProvider<M, E, F, V>,
            modelToStartFrom: M,
            init: Init<M, F>?,
            workRunner: WorkRunner,
            maxEffectsToQueue: Int
        ): MobiusLoopViewModel<M, E, F, V> {
            return MobiusLoopViewModel(
                loopFactoryProvider,
                modelToStartFrom,
                init,
                workRunner,
                maxEffectsToQueue
            )
        }
    }
}
