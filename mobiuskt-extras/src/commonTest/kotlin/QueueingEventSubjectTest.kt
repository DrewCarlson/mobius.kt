package kt.mobius.extras

import kt.mobius.test.RecordingConsumer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class QueueingEventSubjectTest {

    private lateinit var eventSubject: QueueingEventSubject<String>
    private lateinit var receiver: RecordingConsumer<String>

    @BeforeTest
    fun setUp() {
        eventSubject = QueueingEventSubject(3)
        receiver = RecordingConsumer()
    }

    @Test
    fun shouldForwardEventsWhenSubscribed() {
        eventSubject.subscribe(receiver)
        eventSubject.accept("hey")
        receiver.assertValues("hey")
    }

    @Test
    fun shouldQueueEventsWhenNotSubscribed() {
        eventSubject.accept("hi")
        eventSubject.accept("ho")
        eventSubject.accept("to the mines we go")
        eventSubject.subscribe(receiver)
        receiver.assertValues("hi", "ho", "to the mines we go")
    }

    @Test
    fun shouldStopSendingEventsWhenSubscriptionDisposed() {
        val subscription = eventSubject.subscribe(receiver)
        eventSubject.accept("a")
        eventSubject.accept("b")
        subscription.dispose()
        eventSubject.accept("don't want to see this")
        receiver.assertValues("a", "b")
    }

    @Test
    fun shouldOnlySupportASingleSubscriber() {
        eventSubject.subscribe(receiver)
        assertFailsWith<IllegalStateException> {
            eventSubject.subscribe(RecordingConsumer())
        }
    }

    @Test
    fun shouldSupportUnsubscribeAndResubscribe() {
        eventSubject.accept("a")
        val subscription = eventSubject.subscribe(receiver)
        eventSubject.accept("b")
        subscription.dispose()
        eventSubject.accept("c")
        eventSubject.subscribe(receiver)
        receiver.assertValues("a", "b", "c")
    }

    @Test
    fun shouldThrowWhenCapacityExceeded() {
        eventSubject.accept("a")
        eventSubject.accept("b")
        eventSubject.accept("c")
        assertFailsWith<IllegalStateException> {
            eventSubject.accept("noo, too many things")
        }
    }

    @Test
    fun unsubscribeShouldBeIdempotent() {
        // given a subscription that has been disposed
        val subscription1 = eventSubject.subscribe(receiver)
        subscription1.dispose()

        // and a new subscription has been created
        val subscription2 = eventSubject.subscribe(receiver)

        // when the first subscription is disposed again
        subscription1.dispose()

        // then the second subscription is not disposed
        eventSubject.accept("a")

        // and disposing the second subscription works
        subscription2.dispose()
        eventSubject.accept("b")
        receiver.assertValues("a")
    }
}