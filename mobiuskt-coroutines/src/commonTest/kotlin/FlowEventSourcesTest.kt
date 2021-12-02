package kt.mobius.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlowEventSourcesTest {

    @Test
    fun eventsAreForwardedInOrder() = runBlocking {
        val source = flowOf(1, 2, 3).toEventSource(this)
        val consumer = RecordingConsumer<Int>()

        source.subscribe(consumer)

        yield()
        consumer.assertValues(1, 2, 3)
    }

    @Test
    fun disposePreventsFurtherEvents() = runBlocking {
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
}
