package kt.mobius.flow

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kt.mobius.test.RecordingConsumer
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class FlowSubtypeEffectHandlerTest {

    private lateinit var consumer: RecordingConsumer<Effect.Test3>
    private lateinit var action: RecordingConsumer<Effect.Test4>
    private lateinit var handler: FlowTransformer<Effect, Event>

    private interface IEffect

    private sealed class Effect {
        data class Test1(val value: Int) : Effect()
        data class Test2(val value: Int) : Effect(), IEffect
        data class Test3(val value: Int) : Effect()
        object Test4 : Effect()
        object Test5 : Effect()

        object Ignored : Effect()
    }

    private sealed class Event {
        data class Test1(val value: Int) : Event()
        data class Test2(val value: Int) : Event()
    }

    @BeforeTest
    fun setup() {
        consumer = RecordingConsumer()
        action = RecordingConsumer()
        handler = subtypeEffectHandler {
            addTransformer<Effect.Test1> { effects -> effects.map { Event.Test1(it.value) } }
            addFunction<Effect.Test2> { Event.Test2(it.value) }
            addConsumer(consumer::accept)
            addAction<Effect.Test4> { action.accept(Effect.Test4) }
            addAction<Effect.Test5> { throw CancellationException("") }
        }
    }

    @Test
    fun testTransformer() = runTest {
        handler(flowOf(Effect.Test1(123))).test {
            assertEquals(Event.Test1(123), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testFunction() = runTest {
        handler(flowOf(Effect.Test2(42))).test {
            assertEquals(Event.Test2(42), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testConsumer() = runTest {
        handler(flowOf(Effect.Test3(321))).collect()

        consumer.assertValues(Effect.Test3(321))
    }

    @Test
    fun testAction() = runTest {
        handler(flowOf(Effect.Test4)).collect()

        action.assertValues(Effect.Test4)
    }

    @Test
    fun testUnhandledEffect() = runTest {
        handler(flowOf(Effect.Ignored)).test {
            assertIs<UnknownEffectException>(awaitError())
        }
    }

    @Test
    fun testCancellationIsIgnored() = runTest {
        handler(flowOf(Effect.Test5)).test {
            awaitComplete()
        }
    }

    @Test
    fun testCannotAttachDuplicateHandler() {
        subtypeEffectHandler<Effect, Event> {
            addAction<Effect.Test4> { }
            assertFailsWith<IllegalArgumentException> {
                addAction<Effect.Test4> { }
            }
        }
    }

    @Test
    fun testIgnoredTypeIsIgnored() = runTest {
        val handler = subtypeEffectHandler<Effect, Event> {
            ignore<Effect.Ignored>()
        }
        handler(flowOf(Effect.Ignored)).test {
            awaitComplete()
        }
    }

    @Test
    fun testAddValueCollector() = runTest {
        val handler = subtypeEffectHandler<Effect, Event> {
            addValueCollector<Effect.Test4> {
                repeat(4) { i ->
                    emit(Event.Test1(i))
                }
            }
        }
        val effectFlow = MutableSharedFlow<Effect>()
        handler(effectFlow).test {
            effectFlow.emit(Effect.Test4)
            repeat(4) { i ->
                assertEquals(i, (awaitItem() as Event.Test1).value)
            }
        }
    }

    @Test
    fun testHandlerErrorIsWrappedAndThrown() = runTest {
        val handler = subtypeEffectHandler<Effect, Event> {
            addAction<Effect.Test4> { error("This should fail.") }
        }

        handler(flowOf(Effect.Test4)).test {
            assertFailsWith<UnrecoverableIncomingException> {
                throw awaitError()
            }
        }
    }

    @Test
    fun testLatestExecutionPolicy() = runTest {
        val handler = subtypeEffectHandler<Effect, Int> {
            addValueCollector<Effect.Test3>(ExecutionPolicy.Latest) { effect ->
                delay(effect.value.toLong())
                emit(effect.value)
            }
        }
        val effectFlow = flowOf(
            Effect.Test3(5),
            Effect.Test3(1)
        ).onEach { delay(1) }
        handler(effectFlow).test {
            advanceTimeBy(1)
            expectNoEvents()
            advanceTimeBy(1)
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testSequentialExecutionPolicy() = runTest {
        val handler = subtypeEffectHandler<Effect, Long> {
            addValueCollector<Effect.Test4>(ExecutionPolicy.Sequential) {
                emit(currentTime)
                delay(1)
            }
        }
        val effectFlow = List(4) { Effect.Test4 }.asFlow()
        handler(effectFlow).test {
            repeat(4) { i ->
                assertEquals(i.toLong(), awaitItem())
            }
        }
    }

    @Test
    fun testConcurrentExecutionPolicy() = runTest {
        val handler = subtypeEffectHandler<Effect, Long> {
            addValueCollector<Effect.Test4>(ExecutionPolicy.Concurrent(4)) {
                emit(currentTime)
                delay(1)
            }
        }
        val effectFlow = List(4) { Effect.Test4 }.asFlow()
        handler(effectFlow).test {
            repeat(4) { i ->
                assertEquals(0, awaitItem())
            }
            awaitComplete()
        }
    }

    @Test
    fun testThrottleLatestExecutionPolicy() = runTest {
        val handler = subtypeEffectHandler<Effect, Pair<Int, Long>> {
            addValueCollector<Effect.Test1>(ExecutionPolicy.ThrottleLatest(1.seconds)) { (index) ->
                emit(index to currentTime)
            }
        }
        val effectFlow = List(4) { Effect.Test1(it) }.asFlow()
        handler(effectFlow).test {
            assertEquals(0 to 0L, awaitItem())
            assertEquals(3 to 1000L, awaitItem())
            awaitComplete()
        }
    }
}
