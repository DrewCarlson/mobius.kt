package kt.mobius

import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.DefaultWorkRunners
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public object Mobius {

    @Suppress("ClassName")
    private object NOOP_EVENT_SOURCE : Connectable<Any, Any> {
        override fun connect(output: Consumer<Any>): Connection<Any> {
            return object : Connection<Any> {
                override fun accept(value: Any) = Unit
                override fun dispose() = Unit
            }
        }
    }

    @Suppress("ClassName")
    private object NOOP_LOGGER : MobiusLoop.Logger<Any, Any, Any> {
        private val logger = MobiusHooks.newLogger("NOOP_LOGGER")

        override fun beforeInit(model: Any) {
            /* noop */
        }

        override fun afterInit(model: Any, result: First<Any, Any>) {
            /* noop */
        }

        override fun exceptionDuringInit(model: Any, exception: Throwable) {
            logger.error(
                exception,
                "error initialising from model: '{}' - {}",
                model,
                exception
            )
        }

        override fun beforeUpdate(model: Any, event: Any) {
            /* noop */
        }

        override fun afterUpdate(model: Any, event: Any, result: Next<Any, Any>) {
            /* noop */
        }

        override fun exceptionDuringUpdate(model: Any, event: Any, exception: Throwable) {
            logger.error(
                exception,
                "error updating model: '{}' with event: '{}' - {}",
                model,
                event,
                exception
            )
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
    public fun <M, E, F> loop(
        update: Update<M, E, F>,
        effectHandler: Connectable<F, E>
    ): MobiusLoop.Builder<M, E, F> {
        return Builder(
            update,
            effectHandler,
            null,
            NOOP_EVENT_SOURCE as Connectable<M, E>,
            DefaultWorkRunners.eventWorkRunnerProducer(),
            DefaultWorkRunners.effectWorkRunnerProducer(),
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
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M
    ): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, null, ImmediateWorkRunner())
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
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        modelRunner: WorkRunner
    ): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, null, modelRunner)
    }

    /**
     * Create a [MobiusLoop.Controller] that allows you to start, stop, and restart MobiusLoops.
     *
     * @param loopFactory a factory for creating loops
     * @param defaultModel the model the controller should start from
     * @param init the init function to run when a loop starts
     * @return a new controller
     */
    @JsName("controllerWithInit")
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        init: Init<M, F>
    ): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, init, ImmediateWorkRunner())
    }

    /**
     * Create a [MobiusLoop.Controller] that allows you to start, stop, and restart MobiusLoops.
     *
     * @param loopFactory a factory for creating loops
     * @param defaultModel the model the controller should start from
     * @param init the init function to run when a loop starts
     * @param modelRunner the WorkRunner to use when observing model changes
     * @return a new controller
     */
    @JsName("controllerWithInitAndModelRunner")
    public fun <M, E, F> controller(
        loopFactory: MobiusLoop.Factory<M, E, F>,
        defaultModel: M,
        init: Init<M, F>,
        modelRunner: WorkRunner
    ): MobiusLoop.Controller<M, E> {
        return MobiusLoopController(loopFactory, defaultModel, init, modelRunner)
    }

    internal data class Builder<M, E, F>(
        private val update: Update<M, E, F>,
        private val effectHandler: Connectable<F, E>,
        private val init: Init<M, F>?,
        private val eventSource: Connectable<M, E>,
        private val eventRunner: Producer<WorkRunner>,
        private val effectRunner: Producer<WorkRunner>,
        internal val logger: MobiusLoop.Logger<M, E, F>
    ) : MobiusLoop.Builder<M, E, F> {

        @Suppress("OverridingDeprecatedMember", "OVERRIDE_DEPRECATION")
        override fun init(init: Init<M, F>): MobiusLoop.Builder<M, E, F> {
            return copy(init = init)
        }

        override fun eventSource(eventSource: EventSource<E>): MobiusLoop.Builder<M, E, F> {
            return copy(eventSource = EventSourceConnectable.create(eventSource))
        }

        override fun eventSources(vararg eventSources: EventSource<E>): MobiusLoop.Builder<M, E, F> {
            return copy(eventSource = EventSourceConnectable.create(MergedEventSource.from(*eventSources)))
        }

        override fun eventSource(eventSource: Connectable<M, E>): MobiusLoop.Builder<M, E, F> {
            return copy(eventSource = eventSource)
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
            var firstModel = startModel
            var firstEffects = emptySet<F>()

            if (init != null) {
                val loggingInit = LoggingInit(init, logger)
                val first = loggingInit.init(startModel)

                firstModel = first.model()
                firstEffects = first.effects()
            }

            return startFromInternal(firstModel, firstEffects)
        }

        override fun startFrom(startModel: M, startEffects: Set<F>): MobiusLoop<M, E, F> {
            check(init == null) { "cannot pass in start effects when a loop has init defined" }

            return startFromInternal(startModel, startEffects)
        }

        private fun startFromInternal(startModel: M, startEffects: Set<F>): MobiusLoop<M, E, F> {
            val loggingUpdate = LoggingUpdate(update, logger)
            return MobiusLoop.create(
                loggingUpdate,
                startModel,
                startEffects,
                effectHandler,
                eventSource,
                eventRunner.get(),
                effectRunner.get()
            )
        }
    }
}
