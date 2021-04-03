package kt.mobius.flow

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class DiscardAfterDisposeConnectableTest {
    private lateinit var recordingConsumer: RecordingConsumer<String>
    private lateinit var testConnection: TestConnection
    private lateinit var underTest: DiscardAfterDisposeConnectable<Int, String>
    private lateinit var connection: Connection<Int>

    @Before
    fun setUp() {
        recordingConsumer = RecordingConsumer()
        testConnection = TestConnection(recordingConsumer)
        underTest = DiscardAfterDisposeConnectable(Connectable { testConnection })
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
        Assert.assertEquals(true, testConnection.disposed)
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

        override fun accept(effect: Int) {
            eventConsumer.accept("Value is: $effect")
        }

        override fun dispose() {
            disposed = true
        }
    }
}
