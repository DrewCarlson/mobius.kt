package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


open class EventSourceConnectableTest {
    lateinit var source: TestEventSource
    lateinit var underTest: Connectable<Int, String>
    lateinit var events: RecordingConsumer<String>

    @BeforeTest
    fun setUp() {
        source = TestEventSource()
        underTest = EventSourceConnectable.create(source)
        events = RecordingConsumer()
    }

    class SubscriptionsBehavior : EventSourceConnectableTest() {
        @Test
        fun subscribesToEventSourceOnConnect() {
            underTest.connect(events)
            assertEquals(1, source.subscriberCount())
        }

        @Test
        fun subscribesToEventSourceOnEveryConnect() {
            val c1 = underTest.connect(events)
            val c2 = underTest.connect(events)
            assertEquals(2, source.subscriberCount())
            c2.dispose()
            assertEquals(1, source.subscriberCount())
            c1.dispose()
            assertEquals(0, source.subscriberCount())
        }

        @Test
        fun disposingUnsubscribesFromEventSource() {
            val connection = underTest.connect(events)
            connection.dispose()
            assertEquals(0, source.subscriberCount())
        }

        @Test
        fun disposingThenSubscribingResubscribesToEventSource() {
            var connection = underTest.connect(events)
            assertEquals(1, source.subscriberCount())
            connection.dispose()
            assertEquals(0, source.subscriberCount())
            connection = underTest.connect(events)
            assertEquals(1, source.subscriberCount())
            connection.dispose()
            assertEquals(0, source.subscriberCount())
        }
    }

    class EmissionsBehavior : EventSourceConnectableTest() {
        @Test
        fun forwardsAllEmittedEvents() {
            underTest.connect(events)
            source.publishEvent("Hello")
            source.publishEvent("World")
            events.assertValues("Hello", "World")
        }

        @Test
        fun noItemsAreEmittedOnceDisposed() {
            val connection: Connection<Int> = underTest.connect(events)
            source.publishEvent("Hello")
            connection.dispose()
            source.publishEvent("World")
            events.assertValues("Hello")
        }
    }

    class TestEventSource : EventSource<String> {
        private val consumers = arrayListOf<Consumer<String>>()

        override fun subscribe(eventConsumer: Consumer<String>): Disposable {
            consumers.add(eventConsumer)
            return Disposable { consumers.remove(eventConsumer) }
        }

        fun publishEvent(event: String) {
            for (consumer in consumers) {
                consumer.accept(event)
            }
        }

        fun subscriberCount(): Int {
            return consumers.size
        }
    }
}