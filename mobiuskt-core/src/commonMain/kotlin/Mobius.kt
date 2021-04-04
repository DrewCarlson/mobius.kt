package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.DefaultWorkRunners
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

object Mobius {
    private object NOOP_INIT : Init<Any, Any> {
        override fun init(model: Any): First<Any, Any> {
            return First.first(model)
        }
    }

    private object NOOP_EVENT_SOURCE : EventSource<Any> {
        override fun subscribe(eventConsumer: Consumer<Any>): Disposable {
            return Disposable { }
        }
    }

    private object NOOP_LOGGER : MobiusLoop.Logger<Any, Any, Any> {
        override fun beforeInit(model: Any) {
            /* noop */
        }

        override fun afterInit(model: Any, result: First<Any, Any>) {
            /* noop */
        }

        override fun exceptionDuringInit(model: Any, exception: Throwable) {
            println("error initialising from model: '$model' - $exception")
            println(exception)
        }

        override fun beforeUpdate(model: Any, event: Any) {
            /* noop */
        }

        override fun afterUpdate(model: Any, event: Any, result: Next<Any, Any>) {
            /* noop */
        }

        override fun exceptionDuringUpdate(model: Any, event: Any, exception: Throwable) {
            println("error updating model: '$model' with event: '$event' - $exception")
            println(exception)
        }
    }

    /**
     * Create a [MobiusLoop.Builder] to help you configure a [MobiusLoop] before starting it.
     *
     * Once done configuring the loop you can start the loop using [MobiusLoop.Factory.startFrom]
     *
     * @param update the [Update] function of the loop
     * @param effectHandler the [Connectable] effect handler of the loop
     * @return a [MobiusLoop.Builder] instance that you can further configure before starting the loop
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @JsName("loop")
    fun <M, E, F> loop(
        update: Update<M, E, F>,
        effectHandler: Connectable<F, E>
    ): MobiusLoop.Builder<M, E, F> {
        val defaultWorkRunners = DefaultWorkRunners()
        return Builder(
            update,
            effectHandler,
            NOOP_INIT as Init<M, F>,
            NOOP_EVENT_SOURCE as EventSource<E>,
            defaultWorkRunners.eventWorkRunnerProducer(),
            defaultWorkRunners.effectWorkRunnerProducer(),
            NOOP_LOGGER as MobiusLoop.Logger<M, E, F>
        )
    }

    /**
     * Create a [MobiusLoop.Controller] that allows you to start, stop, and restart MobiusLoops.
     *
     * @param loopFactory a factory for creating loops
     * @param defaultModel the model the controller should start from
     * @return a new controller
     */
    @JvmStatic
    @JsName("controller")
    fun <M, E, F> controller(loopFactory: MobiusLoop.Factory<M, E, F>, defaultModel: M): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, ImmediateWorkRunner())
    }

    /**
     * Create a [MobiusLoop.Controller] that allows you to start, stop, and restart MobiusLoops.
     *
     * @param loopFactory a factory for creating loops
     * @param defaultModel the model the controller should start from
     * @param modelRunner the WorkRunner to use when observing model changes
     * @return a new controller
     */
    @JvmStatic
    @JsName("controllerWithModelRunner")
    fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        modelRunner: WorkRunner
    ): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, modelRunner)
    }

    data class Builder<M, E, F>(
        private val update: Update<M, E, F>,
        private val effectHandler: Connectable<F, E>,
        private val init: Init<M, F>,
        private val eventSource: EventSource<E>,
        private val eventRunner: Producer<WorkRunner>,
        private val effectRunner: Producer<WorkRunner>,
        private val logger: MobiusLoop.Logger<M, E, F>
    ) : MobiusLoop.Builder<M, E, F> {

        override fun init(init: Init<M, F>): MobiusLoop.Builder<M, E, F> {
            return copy(init = init)
        }

        override fun eventSource(eventSource: EventSource<E>): MobiusLoop.Builder<M, E, F> {
            return copy(eventSource = eventSource)
        }

        override fun eventSources(vararg eventSources: EventSource<E>): MobiusLoop.Builder<M, E, F> {
            return copy(eventSource = MergedEventSource.from(*eventSources))
        }

        override fun logger(logger: MobiusLoop.Logger<M, E, F>): MobiusLoop.Builder<M, E, F> {
            return copy(logger = logger)
        }

        override fun eventRunner(eventRunner: Producer<WorkRunner>): MobiusLoop.Builder<M, E, F> {
            return copy(eventRunner = eventRunner)
        }

        override fun effectRunner(effectRunner: Producer<WorkRunner>): MobiusLoop.Builder<M, E, F> {
            return copy(effectRunner = effectRunner)
        }

        override fun startFrom(startModel: M): MobiusLoop<M, E, F> {
            val loggingInit = LoggingInit(init, logger)
            val loggingUpdate = LoggingUpdate(update, logger)

            return MobiusLoop.create(
                MobiusStore.create(loggingInit, loggingUpdate, startModel),
                effectHandler,
                eventSource,
                eventRunner.get(),
                effectRunner.get()
            )
        }
    }
}
