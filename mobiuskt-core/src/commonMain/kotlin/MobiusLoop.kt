package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.WorkRunner
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.jvm.Volatile


/**
 * This is the main loop for Mobius.
 *
 * It hooks up all the different parts of the main Mobius loop, and dispatches messages
 * internally on the appropriate executors.
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class MobiusLoop<M, E, F> private constructor(
    eventProcessorFactory: EventProcessor.Factory<M, E, F>,
    startModel: M,
    startEffects: Set<F>,
    effectHandler: Connectable<F, E>,
    eventSource: Connectable<M, E>,
    eventRunner: WorkRunner,
    effectRunner: WorkRunner
) : Disposable {

    private enum class RunState {
        RUNNING, // the loop is running normally
        DISPOSING, // the loop is in the process of shutting down
        DISPOSED // the loop has been shut down - any further attempts at interacting with it should be considered to be errors.
    }

    private val lock = SynchronizedObject()

    public companion object {

        @JvmStatic
        @JsName("create")
        public fun <M, E, F> create(
            update: Update<M, E, F>,
            startModel: M,
            startEffects: Set<F>,
            effectHandler: Connectable<F, E>,
            eventSource: Connectable<M, E>,
            eventRunner: WorkRunner,
            effectRunner: WorkRunner
        ): MobiusLoop<M, E, F> {
            return MobiusLoop(
                EventProcessor.Factory(MobiusStore.create(update, startModel)),
                startModel,
                startEffects,
                effectHandler,
                eventSource,
                eventRunner,
                effectRunner
            )
        }
    }

    private val eventDispatcher: MessageDispatcher<E>
    private val effectDispatcher: MessageDispatcher<F>

    private val onEventReceived: DiscardAfterDisposeWrapper<E>
    private val onEffectReceived: DiscardAfterDisposeWrapper<F>

    // NOTE: lateinit var required for out of order delegate creation, only set once
    private lateinit var eventProcessor: EventProcessor<M, E, F>
    private lateinit var effectConsumer: Connection<F>
    private val eventSourceModelConsumer: QueueingConnection<M> = QueueingConnection()

    private var modelObservers = arrayListOf<Consumer<M>>()

    @Volatile
    private var runState = RunState.RUNNING

    @Volatile
    public var mostRecentModel: M = startModel
        private set

    init {
        val onModelChanged = Consumer<M> { model ->
            mostRecentModel = model
            eventSourceModelConsumer.accept(model)

            synchronized(lock) {
                for (observer in modelObservers) {
                    observer.accept(model)
                }
            }
        }

        onEventReceived = DiscardAfterDisposeWrapper.wrapConsumer { event ->
            eventProcessor.update(event)
        }
        onEffectReceived = DiscardAfterDisposeWrapper.wrapConsumer { effect ->
            try {
                effectConsumer.accept(effect)
            } catch (t: Throwable) {
                throw ConnectionException(effect as Any, t)
            }
        }

        eventDispatcher = MessageDispatcher(eventRunner, onEventReceived)
        effectDispatcher = MessageDispatcher(effectRunner, onEffectReceived)
        eventProcessor = eventProcessorFactory.create(effectDispatcher, onModelChanged)

        // NOTE: eventConsumer can be invoked from other languages,
        // using lambda syntax results in the `accept` method being
        // unavailable in Swift/ObjC.
        @Suppress("ObjectLiteralToLambda")
        val eventConsumer: Consumer<E> = object : Consumer<E> {
            override fun accept(value: E) {
                dispatchEvent(value)
            }
        }

        this.effectConsumer = effectHandler.connect(eventConsumer)

        onModelChanged.accept(startModel)
        for (effect in startEffects) {
            effectDispatcher.accept(effect)
        }

        eventSourceModelConsumer.setDelegate(eventSource.connect(eventConsumer))
    }

    public fun dispatchEvent(event: E) {
        check(runState != RunState.DISPOSED) {
            "This loop has already been disposed. You cannot dispatch events after disposal - event received: ${event!!::class.simpleName}=${event}, currentModel: $mostRecentModel"
        }

        // ignore events received while disposing to avoid races during shutdown
        if (runState == RunState.DISPOSING) return

        try {
            eventDispatcher.accept(event)
        } catch (e: RuntimeException) {
            throw IllegalStateException("Exception processing event: $event", e)
        }
    }

    /**
     * Add an observer of model changes to this loop. If [mostRecentModel] is non-null,
     * the observer will immediately be notified of the most recent model. The observer will be
     * notified of future changes to the model until the loop or the returned [Disposable] is
     * disposed.
     *
     * @param observer a non-null observer of model changes
     * @return a [Disposable] that can be used to stop further notifications to the observer
     * @throws IllegalStateException if the loop has been disposed
     */
    @Throws(IllegalStateException::class)
    public fun observe(observer: Consumer<M>): Disposable {
        check(runState != RunState.DISPOSED) {
            "This loop has already been disposed. You cannot observe a disposed loop"
        }

        if (runState == RunState.DISPOSING) return Disposable { }

        val currentModel = mostRecentModel
        // Start by emitting the most recently received model.
        observer.accept(currentModel)

        synchronized(lock) {
            modelObservers = (modelObservers + observer) as ArrayList<Consumer<M>>
        }

        return Disposable {
            synchronized(lock) {
                modelObservers = (modelObservers - observer) as ArrayList<Consumer<M>>
            }
        }
    }

    override fun dispose() {
        synchronized(lock) {
            if (runState == RunState.DISPOSED) return

            runState = RunState.DISPOSING

            modelObservers.clear()

            onEventReceived.dispose()
            onEffectReceived.dispose()

            eventSourceModelConsumer.dispose()
            effectConsumer.dispose()

            eventDispatcher.dispose()
            effectDispatcher.dispose()

            runState = RunState.DISPOSED
        }
    }

    /**
     * Defines a fluent API for configuring a [MobiusLoop]. Implementations must be immutable,
     * making them safe to share between threads.
     *
     * @param M the model type
     * @param E the event type
     * @param F the effect type
     */
    public interface Builder<M, E, F> : Factory<M, E, F> {

        /**
         * @return a new [Builder] with the supplied [Init], and the same values as the
         * current one for the other fields.
         */
        @JsName("init")
        @Deprecated("Pass initial Effects with the start model using Builder.startFrom(model, setOf(Effects)")
        public fun init(init: Init<M, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied [EventSource], and the same values as
         * the current one for the other fields. NOTE: Invoking this method will replace the current
         * [EventSource] with the supplied one. If you want to pass multiple event sources,
         * please use [eventSources].
         */
        @JsName("eventSource")
        public fun eventSource(eventSource: EventSource<E>): Builder<M, E, F>

        /**
         * @return a new [Builder] with an [EventSource] that merges the supplied event
         * sources, and the same values as the current one for the other fields.
         */
        @JsName("eventSources")
        public fun eventSources(vararg eventSources: EventSource<E>): Builder<M, E, F>

        /**
         * Returns a new [Builder] with the supplied [Connectable], and the same values
         * as the current one for the other fields. NOTE: Invoking this method will replace the current
         * event source with the supplied one. If a loop has a [Connectable] as its event
         * source, it will connect to it and will invoke the [Connection] accept method every
         * time the model changes. This allows us to conditionally subscribe to different sources based
         * on the current state. If you provide a regular [EventSource], it will be wrapped in
         * a [Connectable] and that implementation will subscribe to that event source only once
         * when the loop is initialized.
         */
        @JsName("eventSourceConnectable")
        public fun eventSource(eventSource: Connectable<M, E>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied logger, and the same values as the current
         * one for the other fields.
         */
        @JsName("logger")
        public fun logger(logger: Logger<M, E, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied event runner, and the same values as the
         * current one for the other fields.
         */
        @JsName("eventRunner")
        public fun eventRunner(eventRunner: Producer<WorkRunner>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied effect runner, and the same values as the
         * current one for the other fields.
         */
        @JsName("effectRunner")
        public fun effectRunner(effectRunner: Producer<WorkRunner>): Builder<M, E, F>
    }

    public interface Factory<M, E, F> {
        /**
         * Start a [MobiusLoop] using this factory.
         *
         * @param startModel the model that the loop should start from
         * @return the started [MobiusLoop]
         */
        @JsName("startFrom")
        public fun startFrom(startModel: M): MobiusLoop<M, E, F>

        /**
         * Start a {@link MobiusLoop} using this factory.
         *
         * @param startModel the model that the loop should start from
         * @param startEffects the effects that the loop should start with
         * @return the started [MobiusLoop]
         */
        @JsName("startFromWithEffects")
        public fun startFrom(startModel: M, startEffects: Set<F>): MobiusLoop<M, E, F>
    }

    /**
     * Defines a controller that can be used to start and stop MobiusLoops.
     *
     * If a loop is stopped and then started again, the new loop will continue from where the last
     * one left off.
     */
    public interface Controller<M, E> {
        /**
         * Indicates whether this controller is running.
         *
         * @return true if the controller is running
         */
        public val isRunning: Boolean

        /**
         * Get the current model of the loop that this controller is running, or the most recent model
         * if it's not running.
         *
         * @return a model with the state of the controller
         */
        public val model: M

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
        @Throws(IllegalStateException::class)
        public fun connect(view: Connectable<M, E>)

        /**
         * Disconnect UI from this controller.
         *
         * @throws IllegalStateException if the loop is running or if there isn't anything to disconnect
         */
        @Throws(IllegalStateException::class)
        public fun disconnect()

        /**
         * Start a MobiusLoop from the current model.
         *
         * @throws IllegalStateException if the loop already is running or no view has been connected
         */
        @Throws(IllegalStateException::class)
        public fun start()

        /**
         * Stop the currently running MobiusLoop.
         *
         *
         * When the loop is stopped, the last model of the loop will be remembered and used as the
         * first model the next time the loop is started.
         *
         * @throws IllegalStateException if the loop isn't running
         */
        @Throws(IllegalStateException::class)
        public fun stop()

        /**
         * Replace which model the controller should start from.
         *
         * @param model the model with the state the controller should start from
         * @throws IllegalStateException if the loop is running
         */
        @JsName("replaceModel")
        @Throws(IllegalStateException::class)
        public fun replaceModel(model: M)
    }

    /** Interface for logging init and update calls.  */
    public interface Logger<M, E, F> {
        /**
         * Called right before the [Init.init] function is called.
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that will be passed to the init function
         */
        @JsName("beforeInit")
        public fun beforeInit(model: M)

        /**
         * Called right after the [Init.init] function is called.
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that was passed to init
         * @param result the [First] that init returned
         */
        @JsName("afterInit")
        public fun afterInit(model: M, result: First<M, F>)

        /**
         * Called if the [Init.init] invocation throws an exception. This is a programmer
         * error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @JsName("exceptionDuringInit")
        public fun exceptionDuringInit(model: M, exception: Throwable)

        /**
         * Called right before the [Update.update] function is called.
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that will be passed to the update function
         * @param event the event that will be passed to the update function
         */
        @JsName("beforeUpdate")
        public fun beforeUpdate(model: M, event: E)

        /**
         * Called right after the [Update.update] function is called.
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that was passed to update
         * @param event the event that was passed to update
         * @param result the [Next] that update returned
         */
        @JsName("afterUpdate")
        public fun afterUpdate(model: M, event: E, result: Next<M, F>)

        /**
         * Called if the [Update.update] invocation throws an exception. This is a
         * programmer error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @JsName("exceptionDuringUpdate")
        public fun exceptionDuringUpdate(model: M, event: E, exception: Throwable)
    }
}
