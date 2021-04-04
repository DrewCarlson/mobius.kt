package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner
import mpp.ensureNeverFrozen
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

/**
 * This is the main loop for Mobius.
 *
 * It hooks up all the different parts of the main Mobius loop, and dispatches messages
 * internally on the appropriate executors.
 */
class MobiusLoop<M, E, F> private constructor(
    eventProcessorFactory: EventProcessor.Factory<M, E, F>,
    effectHandler: Connectable<F, E>,
    eventSource: EventSource<E>,
    eventRunner: WorkRunner,
    effectRunner: WorkRunner
) : Disposable {

    companion object {

        @JvmStatic
        @JsName("create")
        fun <M, E, F> create(
            store: MobiusStore<M, E, F>,
            effectHandler: Connectable<F, E>,
            eventSource: EventSource<E>,
            eventRunner: WorkRunner,
            effectRunner: WorkRunner
        ): MobiusLoop<M, E, F> {

            return MobiusLoop(
                EventProcessor.Factory(store),
                effectHandler,
                eventSource,
                eventRunner,
                effectRunner
            )
        }
    }

    private val eventDispatcher = MessageDispatcher(eventRunner, Consumer<E> { event ->
        eventProcessor.update(event)
    })
    private val effectDispatcher = MessageDispatcher(effectRunner, Consumer<F> { effect ->
        try {
            effectConsumer.accept(effect)
        } catch (t: Throwable) {
            throw ConnectionException(effect!!, t)
        }
    })

    private val eventProcessor = eventProcessorFactory.create(effectDispatcher) { model ->
        mpp.synchronized(modelObservers) {
            mostRecentModel = model
            for (observer in modelObservers) {
                observer.accept(model)
            }
        }
    }
    private val effectConsumer: Connection<F>
    private val eventSourceDisposable: Disposable

    private val modelObservers = arrayListOf<Consumer<M>>()

    @Volatile
    var mostRecentModel: M? = null
        private set

    @Volatile
    private var disposed: Boolean = false

    init {
        ensureNeverFrozen()

        // NOTE: eventConsumer can be invoked from other languages,
        // using lambda syntax results in the `accept` method being
        // unavailable in Swift/ObjC.
        @Suppress("ObjectLiteralToLambda")
        val eventConsumer: Consumer<E> = object : Consumer<E> {
            override fun accept(value: E) {
                dispatchEvent(value)
            }
        }

        eventConsumer.ensureNeverFrozen()

        this.effectConsumer = effectHandler.connect(eventConsumer)
        this.eventSourceDisposable = eventSource.subscribe(eventConsumer)

        eventRunner.post(
            object : Runnable {
                override fun run() {
                    eventProcessor.init()
                }
            })
    }

    fun dispatchEvent(event: E) {
        if (disposed)
            throw IllegalStateException(
                "This loop has already been disposed. You cannot dispatch events after disposal"
            )
        eventDispatcher.accept(event)
    }

    /**
     * Add an observer of model changes to this loop. If [getMostRecentModel] is non-null,
     * the observer will immediately be notified of the most recent model. The observer will be
     * notified of future changes to the model until the loop or the returned [Disposable] is
     * disposed.
     *
     * @param observer a non-null observer of model changes
     * @return a [Disposable] that can be used to stop further notifications to the observer
     * @throws IllegalStateException if the loop has been disposed
     */
    fun observe(observer: Consumer<M>): Disposable {
        mpp.synchronized(modelObservers) {
            if (disposed) {
                error("This loop has already been disposed. You cannot observe a disposed loop")
            }

            val currentModel = mostRecentModel
            if (currentModel != null) {
                // Start by emitting the most recently received model.
                observer.accept(currentModel)
            }

            modelObservers.add(observer)
        }

        return Disposable {
            mpp.synchronized(modelObservers) {
                modelObservers.remove(observer)
            }
        }
    }

    @Synchronized
    override fun dispose() {
        mpp.synchronized(modelObservers) {
            modelObservers.clear()
        }

        eventDispatcher.disable()
        effectDispatcher.disable()

        eventSourceDisposable.dispose()
        effectConsumer.dispose()

        eventDispatcher.dispose()
        effectDispatcher.dispose()

        disposed = true
    }

    /**
     * Defines a fluent API for configuring a [MobiusLoop]. Implementations must be immutable,
     * making them safe to share between threads.
     *
     * @param M the model type
     * @param E the event type
     * @param F the effect type
     */
    interface Builder<M, E, F> : Factory<M, E, F> {

        /**
         * @return a new [Builder] with the supplied [Init], and the same values as the
         * current one for the other fields.
         */
        @JsName("init")
        fun init(init: Init<M, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied [EventSource], and the same values as
         * the current one for the other fields. NOTE: Invoking this method will replace the current
         * [EventSource] with the supplied one. If you want to pass multiple event sources,
         * please use [eventSources].
         */
        @JsName("eventSource")
        fun eventSource(eventSource: EventSource<E>): Builder<M, E, F>

        /**
         * @return a new [Builder] with an [EventSource] that merges the supplied event
         * sources, and the same values as the current one for the other fields.
         */
        @JsName("eventSources")
        fun eventSources(vararg eventSources: EventSource<E>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied logger, and the same values as the current
         * one for the other fields.
         */
        @JsName("logger")
        fun logger(logger: Logger<M, E, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied event runner, and the same values as the
         * current one for the other fields.
         */
        @JsName("eventRunner")
        fun eventRunner(eventRunner: Producer<WorkRunner>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied effect runner, and the same values as the
         * current one for the other fields.
         */
        @JsName("effectRunner")
        fun effectRunner(effectRunner: Producer<WorkRunner>): Builder<M, E, F>
    }

    interface Factory<M, E, F> {
        /**
         * Start a [MobiusLoop] using this factory.
         *
         * @param startModel the model that the loop should start from
         * @return the started [MobiusLoop]
         */
        @JsName("startFrom")
        fun startFrom(startModel: M): MobiusLoop<M, E, F>
    }

    /**
     * Defines a controller that can be used to start and stop MobiusLoops.
     *
     * If a loop is stopped and then started again, the new loop will continue from where the last
     * one left off.
     */
    interface Controller<M, E> {
        /**
         * Indicates whether this controller is running.
         *
         * @return true if the controller is running
         */
        val isRunning: Boolean

        /**
         * Get the current model of the loop that this controller is running, or the most recent model
         * if it's not running.
         *
         * @return a model with the state of the controller
         */
        val model: M

        /**
         * Connect a view to this controller.
         *
         * Must be called before [start].
         *
         * The [Connectable] will be given an event consumer, which the view should use to send
         * events to the MobiusLoop. The view should also return a [Connection] that accepts
         * models and renders them. Disposing the connection should make the view stop emitting events.
         *
         * The view Connectable is guaranteed to only be connected once, so you don't have to check
         * for multiple connections or throw [ConnectionLimitExceededException].
         *
         * @throws IllegalStateException if the loop is running or if the controller already is
         * connected
         */
        @JsName("connect")
        fun connect(view: Connectable<M, E>)

        /**
         * Disconnect UI from this controller.
         *
         * @throws IllegalStateException if the loop is running or if there isn't anything to disconnect
         */
        fun disconnect()

        /**
         * Start a MobiusLoop from the current model.
         *
         * @throws IllegalStateException if the loop already is running or no view has been connected
         */
        fun start()

        /**
         * Stop the currently running MobiusLoop.
         *
         *
         * When the loop is stopped, the last model of the loop will be remembered and used as the
         * first model the next time the loop is started.
         *
         * @throws IllegalStateException if the loop isn't running
         */
        fun stop()

        /**
         * Replace which model the controller should start from.
         *
         * @param model the model with the state the controller should start from
         * @throws IllegalStateException if the loop is running
         */
        @JsName("replaceModel")
        fun replaceModel(model: M)
    }

    /** Interface for logging init and update calls.  */
    interface Logger<M, E, F> {
        /**
         * Called right before the [Init.init] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that will be passed to the init function
         */
        @JsName("beforeInit")
        fun beforeInit(model: M)

        /**
         * Called right after the [Init.init] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that was passed to init
         * @param result the [First] that init returned
         */
        @JsName("afterInit")
        fun afterInit(model: M, result: First<M, F>)

        /**
         * Called if the [Init.init] invocation throws an exception. This is a programmer
         * error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @JsName("exceptionDuringInit")
        fun exceptionDuringInit(model: M, exception: Throwable)

        /**
         * Called right before the [Update.update] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that will be passed to the update function
         * @param event the event that will be passed to the update function
         */
        @JsName("beforeUpdate")
        fun beforeUpdate(model: M, event: E)

        /**
         * Called right after the [Update.update] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that was passed to update
         * @param event the event that was passed to update
         * @param result the [Next] that update returned
         */
        @JsName("afterUpdate")
        fun afterUpdate(model: M, event: E, result: Next<M, F>)

        /**
         * Called if the [Update.update] invocation throws an exception. This is a
         * programmer error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @JsName("exceptionDuringUpdate")
        fun exceptionDuringUpdate(model: M, event: E, exception: Throwable)
    }
}
