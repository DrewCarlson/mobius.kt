package kt.mobius

import kt.mobius.runners.WorkRunners
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageDispatcherTest {
    private lateinit var messages: MutableList<String>

    @BeforeTest
    fun setUp() {
        messages = ArrayList()
    }

    @Test
    fun shouldForwardMessagesToConsumer() {
        MessageDispatcher(WorkRunners.immediate(), messages::add).accept("hey hello")
        assertEquals(1, messages.size)
        assertEquals("hey hello", messages.first())
    }

    @Test
    fun shouldSendErrorsFromConsumerToMobiusHooks() {
        // given an error handler
        val errorHandler = TestErrorHandler()
        MobiusHooks.setErrorHandler(errorHandler)
        val expected = RuntimeException("boo")

        // and a message consumer that throws an exception,
        // when a message is dispatched
        MessageDispatcher<String>(WorkRunners.immediate()) { throw expected }
            .accept("here's an event that should be reported as the cause of failure")

        // then the exception gets sent to the error handler.
        val firstError = errorHandler.handledErrors.firstOrNull()
        assertEquals(expected, firstError?.cause)
        assertTrue(firstError?.message?.contains("here's an event that should be reported as the cause of failure") ?: false)
    }

    @Test
    fun shouldIgnoreMessagesAfterDispose() {
        // given a message dispatcher that has been disposed
        val messageDispatcher = MessageDispatcher(WorkRunners.immediate(), messages::add)
        messageDispatcher.dispose()

        // when a message arrives
        messageDispatcher.accept("foo")

        // it is ignored
        assertTrue(messages.isEmpty())
    }
}
