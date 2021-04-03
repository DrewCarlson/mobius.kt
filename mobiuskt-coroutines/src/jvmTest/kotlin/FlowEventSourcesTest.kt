package kt.mobius.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FlowEventSourcesTest {

    @Test
    fun eventsAreForwardedInOrder() = runBlockingTest {
        val source = flowOf(1, 2, 3).toEventSource(this)
        val consumer = RecordingConsumer<Int>()

        source.subscribe(consumer)

        //consumer.waitForChange(50)
        consumer.assertValues(1, 2, 3)
    }

    @Test
    fun disposePreventsFurtherEvents() = runBlockingTest {
        val channel = Channel<Int>()
        val source = channel.consumeAsFlow().toEventSource(this)
        val consumer = RecordingConsumer<Int>()

        val subscription = source.subscribe(consumer)

        channel.send(1)
        channel.send(2)
        subscription.dispose()

        //consumer.waitForChange(50)
        consumer.assertValues(1, 2)

        assertTrue(channel.isClosedForSend)
    }
}
