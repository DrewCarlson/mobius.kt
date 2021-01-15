package kt.mobius

import com.google.common.util.concurrent.SettableFuture
import kt.mobius.Effects.effects
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.internal_util.Throwables
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner
import kt.mobius.test.TestWorkRunner
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.After
import org.junit.Before
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.*

class MobiusLoopTest {

    private lateinit var mobiusLoop: MobiusLoop<String, TestEvent, TestEffect>
    private lateinit var mobiusStore: MobiusStore<String, TestEvent, TestEffect>
    private lateinit var effectHandler: Connectable<TestEffect, TestEvent>

    private val immediateRunner = ImmediateWorkRunner()
    private lateinit var backgroundRunner: WorkRunner

    private var eventSource =
        EventSource<TestEvent> {
            Disposable {
            }
        }

    private lateinit var observer: RecordingModelObserver<String>
    private var effectObserver: RecordingConsumer<TestEffect>? = null
    private lateinit var update: Update<String, TestEvent, TestEffect>

    @Before
    fun setUp() {
        backgroundRunner = ExecutorServiceWorkRunner(Executors.newSingleThreadExecutor())
        val init = Init<String, TestEffect> { model ->
            First.first(model)
        }

        update = Update { model, mobiusEvent ->
            when (mobiusEvent) {
                is TestEvent.EventWithCrashingEffect ->
                    Next.next("will crash", effects(TestEffect.Crash))
                is TestEvent.EventWithSafeEffect -> Next.next(
                    "$model->$mobiusEvent", setOf<TestEffect>(TestEffect.SafeEffect(mobiusEvent.toString()))
                )
                else -> Next.next("$model->$mobiusEvent")
            }
        }

        mobiusStore = MobiusStore.create(init, update, "init")

        effectHandler = Connectable {
            SimpleConnection { effect ->
                effectObserver?.accept(effect)
                if (effect is TestEffect.Crash) {
                    throw RuntimeException("Crashing!")
                }
            }
        }

        setupWithEffects(effectHandler, immediateRunner)
    }

    @After
    fun tearDown() {
        backgroundRunner.dispose()
    }

    @Test
    fun shouldTransitionToNextStateBasedOnInput() {
        mobiusLoop.dispatchEvent(TestEvent.Simple("first"))
        mobiusLoop.dispatchEvent(TestEvent.Simple("second"))

        observer.assertStates("init", "init->first", "init->first->second")
    }

    @Test
    fun shouldSurviveEffectPerformerThrowing() {
        mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
        mobiusLoop.dispatchEvent(TestEvent.Simple("should happen"))

        observer.assertStates("init", "will crash", "will crash->should happen")
    }

    @Test
    fun shouldSurviveEffectPerformerThrowingMultipleTimes() {
        mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
        mobiusLoop.dispatchEvent(TestEvent.Simple("should happen"))
        mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
        mobiusLoop.dispatchEvent(TestEvent.Simple("should happen, too"))

        observer.assertStates(
            "init",
            "will crash",
            "will crash->should happen",
            "will crash",
            "will crash->should happen, too"
        )
    }

    @Test
    fun shouldSupportEffectsThatGenerateEvents() {
        setupWithEffects(Connectable { eventConsumer ->
            SimpleConnection { effect ->
                eventConsumer.accept(TestEvent.Simple(effect.toString()))
            }
        }, immediateRunner)

        mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("hi"))

        observer.assertStates("init", "init->hi", "init->hi->effecthi")
    }

    @Test
    fun shouldOrderStateChangesCorrectlyWhenEffectsAreSlow() {
        val future = SettableFuture.create<TestEvent>()

        setupWithEffects(Connectable { eventConsumer ->
            SimpleConnection {

                try {
                    eventConsumer.accept(future.get())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        }, backgroundRunner)

        mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("1"))
        mobiusLoop.dispatchEvent(TestEvent.Simple("2"))

        await().atMost(Duration.ONE_SECOND).until { observer.valueCount() >= 3 }

        future.set(TestEvent.Simple("3"))

        await().atMost(Duration.ONE_SECOND).until { observer.valueCount() >= 4 }
        observer.assertStates("init", "init->1", "init->1->2", "init->1->2->3")
    }

    @Test
    fun shouldSupportHandlingEffectsWhenOneEffectNeverCompletes() {
        setupWithEffects(Connectable { eventConsumer ->
            SimpleConnection { effect ->

                if (effect is TestEffect.SafeEffect) {
                    if (effect.id == "1") {
                        try {
                            // Rough approximation of waiting infinite amount of time.
                            Thread.sleep(2000)
                        } catch (e: InterruptedException) {
                            // ignored.
                        }

                        return@SimpleConnection
                    }
                }
                eventConsumer.accept(TestEvent.Simple(effect.toString()))
            }
        }, ExecutorServiceWorkRunner(Executors.newFixedThreadPool(2)))

        // the effectHandler associated with "1" should never happen
        mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("1"))
        mobiusLoop.dispatchEvent(TestEvent.Simple("2"))
        mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("3"))

        await().atMost(Duration.FIVE_SECONDS).until { observer.valueCount() >= 5 }

        observer.assertStates(
            "init", "init->1", "init->1->2", "init->1->2->3", "init->1->2->3->effect3"
        )
    }

    @Test
    fun shouldPerformEffectFromInit() {
        val init = Init<String, TestEffect> { model ->
            First.first(model, setOf<TestEffect>(TestEffect.SafeEffect("frominit")))
        }

        val update =
            Update<String, TestEvent, TestEffect> { model, event ->
                Next.next("$model->$event")
            }


        mobiusStore = MobiusStore.create(init, update, "init")
        val testWorkRunner = TestWorkRunner()

        setupWithEffects(Connectable { eventConsumer ->
            SimpleConnection { effect ->
                eventConsumer.accept(TestEvent.Simple(effect.toString()))
            }
        }, testWorkRunner)

        //TODO: observer.waitForChange(100)
        testWorkRunner.runAll()

        observer.assertStates("init", "init->effectfrominit")
    }

    @Test
    fun dispatchingEventsAfterDisposalThrowsException() {
        mobiusLoop.dispose()

        assertFailsWith(IllegalStateException::class) {
            mobiusLoop.dispatchEvent(TestEvent.Simple("2"))
        }
    }

    @Test
    fun disposingTheLoopDisposesTheWorkRunners() {
        val eventRunner = TestWorkRunner()
        val effectRunner = TestWorkRunner()

        mobiusLoop =
            MobiusLoop.create(mobiusStore, effectHandler, eventSource, eventRunner, effectRunner)

        mobiusLoop.dispose()

        assertTrue(eventRunner.isDisposed, "expecting event WorkRunner to be disposed")
        assertTrue(effectRunner.isDisposed, "expecting effect WorkRunner to be disposed")
    }

    @Test
    fun shouldSupportUnregisteringObserver() {
        observer = RecordingModelObserver()

        mobiusLoop =
            MobiusLoop.create(
                mobiusStore, effectHandler, eventSource, immediateRunner, immediateRunner
            );

        val unregister = mobiusLoop.observe(observer)

        mobiusLoop.dispatchEvent(TestEvent.Simple("active observer"))
        unregister.dispose()
        mobiusLoop.dispatchEvent(TestEvent.Simple("shouldn't be seen"))

        observer.assertStates("init", "init->active observer")
    }

    @Test
    fun shouldThrowForEventSourceEventsAfterDispose() {
        val eventSource = FakeEventSource<TestEvent>()

        mobiusLoop =
            MobiusLoop.create(
                mobiusStore, effectHandler, eventSource, immediateRunner, immediateRunner
            )

        observer = RecordingModelObserver() // to clear out the init from the previous setup
        mobiusLoop.observe(observer)

        eventSource.emit(TestEvent.EventWithSafeEffect("one"))
        mobiusLoop.dispose()


        assertFailsWith(IllegalStateException::class) {
            eventSource.emit(TestEvent.EventWithSafeEffect("two"))
        }

        observer.assertStates("init", "init->one")
    }

    @Test
    fun shouldThrowForEffectHandlerEventsAfterDispose() {
        val effectHandler = FakeEffectHandler()

        setupWithEffects(effectHandler, immediateRunner);

        effectHandler.emitEvent(TestEvent.EventWithSafeEffect("good one"))

        mobiusLoop.dispose()

        assertFailsWith(IllegalStateException::class) {
            effectHandler.emitEvent(TestEvent.EventWithSafeEffect("bad one"))
        }

        observer.assertStates("init", "init->good one")
    }

    @Test
    fun shouldProcessInitBeforeEventsFromEffectHandler() {
        mobiusStore = MobiusStore.create(Init { First.first("I$it") }, update, "init")

        // when an effect handler that emits events before returning the connection
        setupWithEffects(
            Connectable { output ->
                output.accept(TestEvent.Simple("1"))

                SimpleConnection {
                }
            },
            immediateRunner
        )

        // in this scenario, the init and the first event get processed before the observer
        // is connected, meaning the 'Iinit' state is never seen
        observer.assertStates("Iinit->1")
    }

    @Test
    fun shouldProcessInitBeforeEventsFromEventSource() {
        mobiusStore = MobiusStore.create(Init { First.first("First$it") }, update, "init")

        eventSource =
            EventSource { eventConsumer ->
                eventConsumer.accept(TestEvent.Simple("1"))
                Disposable {
                    // do nothing
                }
            }

        setupWithEffects(FakeEffectHandler(), immediateRunner)

        // in this scenario, the init and the first event get processed before the observer
        // is connected, meaning the 'Firstinit' state is never seen
        observer.assertStates("Firstinit->1")
    }

    @Test
    fun eventsFromEventSourceDuringDisposeAreIgnored() {
        val updateWasCalled = AtomicBoolean()

        val builder = Mobius.loop(Update { _: String, _: TestEvent ->
            updateWasCalled.set(true)
            Next.noChange<String, TestEffect>()
        }, effectHandler)

        builder.eventSource(EmitDuringDisposeEventSource(TestEvent.Simple("bar")))
            .startFrom("foo")
            .dispose()

        assertFalse(updateWasCalled.get())
    }

    @Test
    fun eventsFromEffectHandlerDuringDisposeAreIgnored() {
        val updateWasCalled = AtomicBoolean()

        val builder = Mobius.loop(Update { _: String, _: TestEvent ->
            updateWasCalled.set(true)
            Next.noChange<String, TestEffect>()
        }, EmitDuringDisposeEffectHandler())

        builder.startFrom("foo").dispose()

        assertFalse(updateWasCalled.get())
    }

    @Test
    fun disposingLoopWhileInitIsRunningDoesNotEmitNewState() {
        // Model changes emitted from the init function during dispose should be ignored.

        // This test will start a loop and wait until (using the initRequested semaphore) the runnable
        // that runs Init is posted to the event runner. The init function will then be blocked using
        // the initLock semaphore. At this point, we proceed to add the observer then dispose of the
        // loop. The loop is setup with an event source that returns a disposable that will unlock
        // init when it is disposed. So when we dispose of the loop, that will unblock init as part of
        // the disposal procedure. The test then waits until the init runnable has completed running.
        // Completion of the init runnable means:
        // a) init has returned a First
        // b) that first has been unpacked and the model has been set on the store
        // c) that model has been passed back to the loop to be emitted to any state observers
        // Since we're in the process of disposing of the loop, we should see no states in our observer
        observer = RecordingModelObserver()
        val initLock = Semaphore(0)
        val initRequested = Semaphore(0)
        val initFinished = Semaphore(0)

        val update = Update { _: String, _: TestEvent ->
            Next.noChange<String, TestEffect>()
        }
        val builder =
            Mobius.loop(update, effectHandler)
                .init(
                    Init { m ->
                        initLock.acquireUninterruptibly()
                        First.first(m)
                    })
                .eventSource(EventSource { Disposable(initLock::release) })
                .eventRunner(Producer {
                    object : WorkRunner {
                        override fun post(runnable: Runnable) {
                            backgroundRunner.post(Runnable {
                                initRequested.release()
                                runnable.run()
                                initFinished.release()
                            })
                        }

                        override fun dispose() {
                            backgroundRunner.dispose()
                        }
                    }
                })
        mobiusLoop = builder.startFrom("foo")
        initRequested.acquireUninterruptibly()
        mobiusLoop.observe(observer)
        mobiusLoop.dispose()
        initFinished.acquireUninterruptibly(1)
        observer.assertStates()
    }

    @Test
    fun disposingLoopBeforeInitRunsIgnoresModelFromInit() {
        // Model changes emitted from the init function during dispose should be ignored.
        // This test sets up the following scenario:
        // 1. The loop is created and initialized on a separate thread
        // 2. The loop is configured with an event runner that will block before executing the init function
        // 3. The test will then dispose of the loop
        // 4. Once the loop is disposed, the test will proceed to unblock the initialization runnable
        // 5. Once the initialization is completed, the test will proceed to examine the observer

        observer = RecordingModelObserver()

        val awaitInitExecutionRequest = Semaphore(0)
        val blockInitExecution = Semaphore(0)
        val initExecutionCompleted = Semaphore(0)

        val update = Update { _: String, _: TestEvent ->
            Next.noChange<String, TestEffect>()
        }
        val builder =
            Mobius.loop(update, effectHandler)
                .eventRunner(
                    Producer {
                        object : WorkRunner {
                            override fun post(runnable: Runnable) {
                                backgroundRunner.post(Runnable {
                                    awaitInitExecutionRequest.release()
                                    blockInitExecution.acquireUninterruptibly()
                                    runnable.run()
                                    initExecutionCompleted.release()
                                })
                            }

                            override fun dispose() {
                                backgroundRunner.dispose()
                            }
                        }
                    })

        Thread { mobiusLoop = builder.startFrom("foo") }.start()

        awaitInitExecutionRequest.acquireUninterruptibly()

        mobiusLoop.observe(observer)
        mobiusLoop.dispose()

        blockInitExecution.release()
        initExecutionCompleted.acquireUninterruptibly()

        observer.assertStates()
    }

    @Test
    fun modelsFromUpdateDuringDisposeAreIgnored() {
        // Model changes emitted from the update function during dispose should be ignored.

        observer = RecordingModelObserver()
        val lock = Semaphore(0)

        val update = Update { _: String, _: TestEvent ->
            lock.acquireUninterruptibly()
            Next.next<String, TestEffect>("baz")
        }

        val builder = Mobius.loop(update, effectHandler)
            .eventRunner(Producer { InitImmediatelyThenUpdateConcurrentlyWorkRunner.create(backgroundRunner) })

        mobiusLoop = builder.startFrom("foo")
        mobiusLoop.observe(observer)

        mobiusLoop.dispatchEvent(TestEvent.Simple("bar"))
        releaseLockAfterDelay(lock, 30)
        mobiusLoop.dispose()

        observer.assertStates("foo")
    }

    @Test
    fun effectsFromUpdateDuringDisposeAreIgnored() {
        // Effects emitted from the update function during dispose should be ignored.

        effectObserver = RecordingConsumer()
        val lock = Semaphore(0)

        val builder = Mobius.loop(
            Update { _: String, _: TestEvent ->
                lock.acquireUninterruptibly()
                Next.dispatch<String, TestEffect>(effects(TestEffect.SafeEffect("baz")))
            },
            effectHandler
        )

        mobiusLoop = builder.startFrom("foo")

        mobiusLoop.dispatchEvent(TestEvent.Simple("bar"))
        releaseLockAfterDelay(lock, 45)
        mobiusLoop.dispose()

        effectObserver!!.assertValues()
    }

    private fun setupWithEffects(
        effectHandler: Connectable<TestEffect, TestEvent>, effectRunner: WorkRunner
    ) {
        observer = RecordingModelObserver()

        mobiusLoop =
            MobiusLoop.create(mobiusStore, effectHandler, eventSource, immediateRunner, effectRunner)

        mobiusLoop.observe(observer)
    }

    private fun releaseLockAfterDelay(lock: Semaphore, delay: Int) {
        Thread {
            try {
                Thread.sleep(delay.toLong())
            } catch (e: InterruptedException) {
                throw Throwables.propagate(e)
            }

            lock.release()
        }.start()
    }

    private sealed class TestEvent(
        private val name: String
    ) {

        class Simple(name: String) : TestEvent(name)

        object EventWithCrashingEffect : TestEvent("crash!")

        class EventWithSafeEffect(id: String) : TestEvent(id)

        override fun toString(): String {
            return name
        }
    }

    private sealed class TestEffect {
        object Crash : TestEffect()

        data class SafeEffect(
            val id: String
        ) : TestEffect() {

            override fun toString(): String {
                return "effect$id"
            }
        }
    }

    private class FakeEffectHandler : Connectable<TestEffect, TestEvent> {

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

    private class EmitDuringDisposeEventSource(private val event: TestEvent) : EventSource<TestEvent> {

        override fun subscribe(eventConsumer: Consumer<TestEvent>): Disposable {
            return Disposable { eventConsumer.accept(event) }
        }
    }

    private class EmitDuringDisposeEffectHandler : Connectable<TestEffect, TestEvent> {

        override fun connect(eventConsumer: Consumer<TestEvent>): Connection<TestEffect> {
            return object : Connection<TestEffect> {
                override fun accept(value: TestEffect) {
                    // ignored
                }

                override fun dispose() {
                    eventConsumer.accept(TestEvent.Simple("bar"))
                }
            }
        }
    }

    @Test
    fun shouldDisposeMultiThreadedEventSourceSafely() {
        // event source that just pushes stuff every X ms on a thread.

        val source = RecurringEventSource()

        val builder = Mobius.loop(update, effectHandler).eventSource(source)

        val random = Random()

        for (i in 0..99) {
            mobiusLoop = builder.startFrom("foo")

            Thread.sleep(random.nextInt(30).toLong())

            mobiusLoop.dispose()
        }
    }

    private class RecurringEventSource : EventSource<TestEvent> {

        internal val completion = SettableFuture.create<Void>()

        override fun subscribe(eventConsumer: Consumer<TestEvent>): Disposable {
            if (completion.isDone) {
                try {
                    completion.get() // should throw since the only way it can complete is exceptionally
                } catch (e: InterruptedException) {
                    throw RuntimeException("handle this", e)
                } catch (e: ExecutionException) {
                    throw RuntimeException("handle this", e)
                }
            }

            val generator = Generator(eventConsumer)

            val t = Thread(generator)
            t.start()

            return Disposable {
                generator.generate = false
                try {
                    t.join()
                } catch (e: InterruptedException) {
                    throw Throwables.propagate(e)
                }
            }
        }

        private inner class Generator(private val consumer: Consumer<TestEvent>) : Runnable {

            @Volatile
            var generate = true

            override fun run() {
                while (generate) {
                    try {
                        consumer.accept(TestEvent.Simple("hi"))
                        Thread.sleep(15)
                    } catch (e: Exception) {
                        completion.setException(e)
                    }
                }
            }
        }
    }

    private class InitImmediatelyThenUpdateConcurrentlyWorkRunner private constructor(
        private val delegate: WorkRunner
    ) : WorkRunner {

        private var ranOnce: Boolean = false

        @Synchronized
        override fun post(runnable: Runnable) {
            if (ranOnce) {
                delegate.post(runnable)
                return
            }

            ranOnce = true
            runnable.run()
        }

        override fun dispose() {
            delegate.dispose()
        }

        companion object {

            fun create(eventRunner: WorkRunner): WorkRunner {
                return InitImmediatelyThenUpdateConcurrentlyWorkRunner(eventRunner)
            }
        }
    }
}
