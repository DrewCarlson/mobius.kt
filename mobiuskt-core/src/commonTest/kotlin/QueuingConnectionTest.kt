package kt.mobius

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class QueuingConnectionTest {
    private lateinit var connection: QueuingConnection<String>
    private lateinit var delegate: RecordingConnection<String>

    @BeforeTest
    fun setUp() {
        delegate = RecordingConnection()
        connection = QueuingConnection()
    }

    @Test
    fun shouldForwardAcceptToDelegateWhenAvailable() {
        connection.setDelegate(delegate)
        connection.accept("hey there")
        connection.accept("hi!")
        delegate.assertValues("hey there", "hi!")
    }

    @Test
    fun shouldQueueAcceptBeforeDelegateAvailable() {
        connection.accept("hey there")
        connection.accept("hi!")

        // nothing yet
        delegate.assertValues()

        // provide delegate and expect the values to show up
        connection.setDelegate(delegate)
        delegate.assertValues("hey there", "hi!")
    }

    @Test
    fun shouldForwardDisposeToDelegate() {
        connection.setDelegate(delegate)
        connection.dispose()
        assertTrue(delegate.disposed)
    }

    @Test
    fun shouldSupportDisposeWithoutDelegate() {
        connection.dispose()
    }

    @Test
    fun shouldNotForwardQueuedValuesAfterDispose() {
        connection.accept("don't want to see this")
        connection.dispose()
        connection.setDelegate(delegate)
        delegate.assertValues()
    }

    @Test
    fun shouldNotAllowDuplicateDelegates() {
        connection.setDelegate(delegate)
        assertFailsWith<IllegalStateException> {
            connection.setDelegate(RecordingConnection())
        }
    }
}