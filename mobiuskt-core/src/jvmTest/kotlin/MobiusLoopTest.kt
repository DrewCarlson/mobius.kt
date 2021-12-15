package kt.mobius

import kt.mobius.Effects.effects
import kt.mobius.MobiusLoop.Companion.create
import kt.mobius.Next.Companion.next
import kt.mobius.functions.Consumer
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kt.mobius.testdomain.*
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


open class MobiusLoopTest {
    lateinit var mobiusLoop: MobiusLoop<String, TestEvent, TestEffect>
    lateinit var effectHandler: Connectable<TestEffect, TestEvent>
    val immediateRunner: WorkRunner = ImmediateWorkRunner()
    lateinit var backgroundRunner: WorkRunner
    var eventSource: Connectable<String, TestEvent> = Connectable {
        object : Connection<String> {
            override fun accept(value: String) {}
            override fun dispose() {}
        }
    }
    lateinit var observer: RecordingModelObserver<String>
    var effectObserver: RecordingConsumer<TestEffect>? = null
    lateinit var update: Update<String, TestEvent, TestEffect>
    lateinit var startModel: String
    lateinit var startEffects: Set<TestEffect>

    @BeforeTest
    fun setUp() {
        backgroundRunner = ExecutorServiceWorkRunner(Executors.newSingleThreadExecutor())
        update = Update { model, mobiusEvent ->
            when (mobiusEvent) {
                is EventWithCrashingEffect -> next("will crash", effects(Crash()))
                is EventWithSafeEffect ->
                    next("$model->$mobiusEvent", effects(SafeEffect(mobiusEvent.toString())))
                else -> next("$model->$mobiusEvent")
            }
        }
        startModel = "init"
        startEffects = emptySet()
        effectHandler = Connectable {
            object : SimpleConnection<TestEffect> {
                override fun accept(value: TestEffect) {
                    effectObserver?.accept(value)
                    if (value is Crash) {
                        throw RuntimeException("Crashing!")
                    }
                }
            }
        }
        setupWithEffects(effectHandler, immediateRunner)
    }

    @AfterTest
    fun tearDown() {
        backgroundRunner.dispose()
    }

    protected fun setupWithEffects(
        effectHandler: Connectable<TestEffect, TestEvent>,
        effectRunner: WorkRunner
    ) {
        observer = RecordingModelObserver()
        mobiusLoop = create(
            update,
            startModel,
            startEffects,
            effectHandler,
            eventSource,
            immediateRunner,
            effectRunner
        )
        mobiusLoop.observe(observer)
    }

    internal class FakeEffectHandler : Connectable<TestEffect, TestEvent> {
        @Volatile
        private var eventConsumer: Consumer<TestEvent>? = null

        fun emitEvent(event: TestEvent) {
            // throws NPE if not connected; that's OK
            eventConsumer!!.accept(event)
        }

        override fun connect(output: Consumer<TestEvent>): Connection<TestEffect> {
            if (eventConsumer != null) {
                throw ConnectionLimitExceededException()
            }

            eventConsumer = output

            return object : Connection<TestEffect> {
                override fun accept(value: TestEffect) {
                    // do nothing
                }

                override fun dispose() {
                    // do nothing
                }
            }
        }
    }
}
