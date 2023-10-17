package kt.mobius.flow

import app.cash.turbine.test
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kt.mobius.EventSource
import kt.mobius.disposables.Disposable
import kt.mobius.test.RecordingConsumer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(DelicateCoroutinesApi::class)
class FlowEventSourcesTest {

    @Test
    fun eventsAreForwardedInOrder() = runTest {
        val source = flowOf(1, 2, 3).toEventSource(this)
        val consumer = RecordingConsumer<Int>()

        source.subscribe(consumer)

        yield()
        consumer.assertValues(1, 2, 3)
    }

    @Test
    fun disposePreventsFurtherEvents() = runTest {
        val channel = Channel<Int>()
        val source = channel.consumeAsFlow().toEventSource(this)
        val consumer = RecordingConsumer<Int>()

        val subscription = source.subscribe(consumer)

        channel.send(1)
        channel.send(2)

        yield() // Wait for events to be received
        subscription.dispose()
        yield() // Wait for disposal to propagate

        consumer.assertValues(1, 2)

        assertTrue(channel.isClosedForSend)
    }

    @Test
    fun eventSourceToFlow() = runTest {
        val eventSource = EventSource { consumer ->
            launch {
                repeat(5) { i ->
                    consumer.accept(i)
                    yield()
                }
            }
            Disposable { }
        }

        val results = eventSource.toFlow()
            .take(5)
            .toList()

        assertEquals(listOf(0, 1, 2, 3, 4), results)
    }

    @Test
    fun eventSourceToFlowDispose() = runTest {
        var disposed = false
        val eventSource = EventSource { consumer ->
            consumer.accept(0)
            Disposable {
                disposed = true
            }
        }

        eventSource.toFlow().test {
            cancelAndIgnoreRemainingEvents()
            assertTrue(disposed)
        }
    }
}
