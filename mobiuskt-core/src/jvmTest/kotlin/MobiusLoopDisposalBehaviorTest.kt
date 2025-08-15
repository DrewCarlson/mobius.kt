package kt.mobius

import com.google.common.util.concurrent.SettableFuture
import kt.mobius.Effects.effects
import kt.mobius.Mobius.loop
import kt.mobius.MobiusLoop.Companion.create
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.WorkRunner
import kt.mobius.runners.WorkRunners
import kt.mobius.test.RecordingConsumer
import kt.mobius.test.RecordingModelObserver
import kt.mobius.test.TestWorkRunner
import kt.mobius.testdomain.EventWithSafeEffect
import kt.mobius.testdomain.SafeEffect
import kt.mobius.testdomain.TestEffect
import kt.mobius.testdomain.TestEvent
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile
import kotlin.test.*


class MobiusLoopDisposalBehavior : MobiusLoopTest() {
    @Test
    fun dispatchingEventsAfterDisposalThrowsException() {
        mobiusLoop.dispose()
        assertFailsWith<IllegalStateException> {
            mobiusLoop.dispatchEvent(TestEvent("2"))
        }
    }

    @Test
    fun disposingTheLoopDisposesTheWorkRunners() {
        val eventRunner = TestWorkRunner()
        val effectRunner = TestWorkRunner()
        mobiusLoop = create(
            update,
            startModel,
            startEffects,
            effectHandler,
            eventSource,
            eventRunner,
            effectRunner
        )
        mobiusLoop.dispose()
        assertTrue(eventRunner.isDisposed, "expecting event WorkRunner to be disposed")
        assertTrue(effectRunner.isDisposed, "expecting effect WorkRunner to be disposed")
    }

    @Test
    fun shouldIncludeEventTypeAndEventAndModelInErrorMessageForEventsAfterDispose() {
        val eventSource = FakeEventSource<TestEvent>()
        mobiusLoop = create(
            update,
            startModel,
            startEffects,
            effectHandler,
            EventSourceConnectable.create(eventSource),
            immediateRunner,
            immediateRunner
        )
        observer = RecordingModelObserver() // to clear out the init from the previous setup
        mobiusLoop.observe(observer)
        eventSource.emit(EventWithSafeEffect("one"))
        mobiusLoop.dispose()
        val event = EventWithSafeEffect("two")
        val error = assertFails { eventSource.emit(event) }
        assertIs<IllegalStateException>(error)
        assertTrue(
            error.message?.contains(event.javaClass.simpleName) ?: false,
            "Expected message to contain '${event::class.simpleName}'"
        )
        assertTrue(
            error.message?.contains(event.toString()) ?: false,
            "Expected message to contain '$event'"
        )
        assertTrue(
            error.message?.contains(mobiusLoop.mostRecentModel) ?: false,
            "Expected message to contain '${mobiusLoop.mostRecentModel}'"
        )
        observer.assertStates("init", "init->one")
    }

    @Test
    fun shouldThrowForEventSourceEventsAfterDispose() {
        val eventSource = FakeEventSource<TestEvent>()
        mobiusLoop = create(
            update,
            startModel,
            startEffects,
            effectHandler,
            EventSourceConnectable.create(eventSource),
            immediateRunner,
            immediateRunner
        )
        observer = RecordingModelObserver() // to clear out the init from the previous setup
        mobiusLoop.observe(observer)
        eventSource.emit(EventWithSafeEffect("one"))
        mobiusLoop.dispose()
        assertFailsWith<IllegalStateException> {
            eventSource.emit(EventWithSafeEffect("two"))
        }
        observer.assertStates("init", "init->one")
    }

    @Test
    fun shouldThrowForEffectHandlerEventsAfterDispose() {
        val effectHandler = FakeEffectHandler()
        setupWithEffects(effectHandler, immediateRunner)
        effectHandler.emitEvent(EventWithSafeEffect("good one"))
        mobiusLoop.dispose()
        assertFailsWith<IllegalStateException> {
            effectHandler.emitEvent(EventWithSafeEffect("bad one"))
        }
        observer.assertStates("init", "init->good one")
    }

    @Test
    fun eventsFromEventSourceDuringDisposeAreIgnored() {
        // Events emitted by the event source during dispose should be ignored.
        val updateWasCalled = AtomicBoolean()
        val builder = loop(
            Update<String, TestEvent, TestEffect> { _, _ ->
                updateWasCalled.set(true)
                noChange()
            },
            effectHandler
        )
        builder
            .eventSource(EmitDuringDisposeEventSource(TestEvent("bar")))
            .startFrom("foo")
            .dispose()
        assertFalse(updateWasCalled.get())
    }

    @Test
    fun eventsFromEffectHandlerDuringDisposeAreIgnored() {
        // Events emitted by the effect handler during dispose should be ignored.
        val updateWasCalled = AtomicBoolean()
        val builder = loop(
            Update<String, TestEvent, TestEffect> { _, _ ->
                updateWasCalled.set(true)
                noChange()
            },
            EmitDuringDisposeEffectHandler()
        )
        builder.startFrom("foo").dispose()
        assertFalse(updateWasCalled.get())
    }

    @Test
    fun modelsFromUpdateDuringDisposeAreIgnored() {
        // Model changes emitted from the update function during dispose should be ignored.
        observer = RecordingModelObserver()
        val lock = Semaphore(0)
        val update = Update<String, TestEvent, TestEffect> { _, _ ->
            lock.acquireUninterruptibly()
            next("baz")
        }
        val builder = loop(
            update,
            effectHandler
        )
        mobiusLoop = builder.startFrom("foo")
        mobiusLoop.observe(observer)
        mobiusLoop.dispatchEvent(TestEvent("bar"))
        releaseLockAfterDelay(lock, 30)
        mobiusLoop.dispose()
        observer.assertStates("foo")
    }

    @Test
    fun effectsFromUpdateDuringDisposeAreIgnored() {
        // Effects emitted from the update function during dispose should be ignored.
        effectObserver = RecordingConsumer()
        val lock = Semaphore(0)
        val builder = loop<String, TestEvent, TestEffect>(
            { _, _ ->
                lock.acquireUninterruptibly()
                Next.dispatch(effects(SafeEffect("baz")))
            },
            effectHandler
        )
        mobiusLoop = builder.startFrom("foo")
        mobiusLoop.dispatchEvent(TestEvent("bar"))
        releaseLockAfterDelay(lock, 30)
        mobiusLoop.dispose()
        effectObserver!!.assertValues()
    }

    @Test
    fun shouldSupportDisposingInObserver() {
        val secondObserver = RecordingModelObserver<String>()

        // ensure there are some observers to iterate over, and that one of them modifies the
        // observer list.
        // ConcurrentModificationException only triggered if three observers added, for some reason
        val disposable = mobiusLoop.observe { }
        mobiusLoop.observe { s: String ->
            if (s.contains("heyho")) {
                disposable.dispose()
            }
        }
        mobiusLoop.observe { }
        mobiusLoop.observe(secondObserver)
        mobiusLoop.dispatchEvent(TestEvent("heyho"))
        secondObserver.assertStates("init", "init->heyho")
    }

    @Test
    fun shouldDisposeMultiThreadedEventSourceSafely() {
        // event source that just pushes stuff every X ms on a thread.
        val source = RecurringEventSource()
        val builder: MobiusLoop.Builder<String, TestEvent, TestEffect> = loop(update, effectHandler).eventSource(source)
        val random = Random()
        for (i in 0..99) {
            mobiusLoop = builder.startFrom("foo")
            Thread.sleep(random.nextInt(30).toLong())
            mobiusLoop.dispose()
        }
    }

    @Test
    fun shouldSafelyDisposeWhenDisposeAndEventsAreOnDifferentThreads() {
        val random = Random()
        val builder: MobiusLoop.Builder<String, TestEvent, TestEffect> = loop(update, effectHandler)
            .eventRunner { WorkRunners.from(Executors.newFixedThreadPool(4)) }
        val thread = Thread {
            for (i in 0..99) {
                mobiusLoop = builder.startFrom("foo")
                try {
                    Thread.sleep(random.nextInt(10).toLong())
                } catch (e: InterruptedException) {
                    throw java.lang.RuntimeException(e)
                }
                mobiusLoop.dispose()
            }
        }
        thread.start()
        for (i in 0..999) {
            try {
                mobiusLoop.dispatchEvent(TestEvent("bar"))
                Thread.sleep(1)
            } catch (e: java.lang.IllegalStateException) {
                if (e.message != null) {
                    assertFalse(e.message!!.startsWith("Exception processing event"))
                }
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        thread.join()
    }

    internal class EmitDuringDisposeEventSource(private val event: TestEvent) : EventSource<TestEvent> {
        override fun subscribe(eventConsumer: Consumer<TestEvent>): Disposable {
            return Disposable { eventConsumer.accept(event) }
        }
    }

    internal class EmitDuringDisposeEffectHandler : Connectable<TestEffect, TestEvent> {
        override fun connect(output: Consumer<TestEvent>): Connection<TestEffect> {
            return object : Connection<TestEffect> {
                override fun accept(value: TestEffect) {
                    // ignored
                }

                override fun dispose() {
                    output.accept(TestEvent("bar"))
                }
            }
        }
    }

    internal class RecurringEventSource : EventSource<TestEvent> {
        val completion = SettableFuture.create<Void>()

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
                    throw RuntimeException(e)
                }
            }
        }

        private inner class Generator(consumer: Consumer<TestEvent>) : Runnable {
            @Volatile
            var generate = true
            private val consumer: Consumer<TestEvent>

            init {
                this.consumer = consumer
            }

            override fun run() {
                while (generate) {
                    try {
                        consumer.accept(TestEvent("hi"))
                        Thread.sleep(15)
                    } catch (e: Exception) {
                        completion.setException(e)
                    }
                }
            }
        }
    }

    companion object {
        fun releaseLockAfterDelay(lock: Semaphore, delay: Int) {
            Thread {
                try {
                    Thread.sleep(delay.toLong())
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                lock.release()
            }
                .start()
        }
    }
}