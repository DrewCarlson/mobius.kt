package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kotlin.test.Test
import kotlin.test.assertTrue

class MergedEventSourceTest {

    @Test
    fun composesMultipleEventSources() {
        val s1 = TestEventSource<String>()
        val s2 = TestEventSource<String>()
        val s3 = TestEventSource<String>()
        val s4 = TestEventSource<String>()

        val mergedSource = MergedEventSource.from(s1, s2, s3, s4)
        val consumer = RecordingConsumer<String>()
        val disposable = mergedSource.subscribe(consumer)

        s1.send("Hello")
        s3.send("World!")
        s2.send("We")
        s4.send("are")
        s1.send("all")
        s2.send("one")
        s3.send("event")
        s1.send("source")

        consumer.assertValues("Hello", "World!", "We", "are", "all", "one", "event", "source")
        disposable.dispose()
        assertTrue(s1.disposed)
        assertTrue(s2.disposed)
        assertTrue(s3.disposed)
        assertTrue(s4.disposed)
    }

    private class TestEventSource<T> : EventSource<T> {

        private var eventConsumer: Consumer<T>? = null
        var disposed: Boolean = false
            private set

        override fun subscribe(eventConsumer: Consumer<T>): Disposable {
            this.eventConsumer = eventConsumer;
            return Disposable {
                disposed = true
                this.eventConsumer = null
            }
        }

        fun send(s: T) {
            eventConsumer!!.accept(s)
        }
    }
}
