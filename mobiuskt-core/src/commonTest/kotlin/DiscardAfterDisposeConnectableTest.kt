package kt.mobius

import kt.mobius.functions.Consumer
import kt.mobius.test.RecordingConsumer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class DiscardAfterDisposeConnectableTest {
    private lateinit var recordingConsumer: RecordingConsumer<String>
    private lateinit var testConnection: TestConnection
    private lateinit var underTest: DiscardAfterDisposeConnectable<Int, String>
    private lateinit var connection: Connection<Int>

    @BeforeTest
    fun setUp() {
        recordingConsumer = RecordingConsumer()
        testConnection = TestConnection(recordingConsumer)
        underTest = DiscardAfterDisposeConnectable { testConnection }
    }

    @Test
    fun forwardsMessagesToWrappedConsumer() {
        connection = underTest.connect(recordingConsumer)
        connection.accept(14)
        recordingConsumer.assertValues("Value is: 14")
    }

    @Test
    fun delegatesDisposeToActualConnection() {
        connection = underTest.connect(recordingConsumer)
        connection.dispose()
        assertEquals(true, testConnection.disposed)
    }

    @Test
    fun discardsEventsAfterDisposal() {
        // given a disposed connection
        connection = underTest.connect(recordingConsumer)
        connection.dispose()

        // when a message arrives
        connection.accept(1)

        // it is discarded
        recordingConsumer.assertValues()
    }

    private class TestConnection(
        private val eventConsumer: Consumer<String>
    ) : Connection<Int> {
        var disposed = false
            private set

        override fun accept(value: Int) {
            eventConsumer.accept("Value is: $value")
        }

        override fun dispose() {
            disposed = true
        }
    }
}
