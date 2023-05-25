package kt.mobius.flow

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kt.mobius.test.RecordingConsumer
import kotlin.test.*

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
            addAction<Effect.Test4> {  }
            assertFailsWith<IllegalArgumentException> {
                addAction<Effect.Test4> {  }
            }
        }
    }
}