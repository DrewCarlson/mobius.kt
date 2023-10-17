package kt.mobius.flow

import kotlinx.coroutines.*
import kt.mobius.Connectable
import kt.mobius.Connection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kt.mobius.test.RecordingConsumer
import kotlin.test.*

@OptIn(DelicateCoroutinesApi::class)
class FlowConnectablesTest {

    private lateinit var input: Channel<String>
    private lateinit var connectable: Connectable<String, Int>

    @BeforeTest
    fun setUp() {
        input = Channel(UNLIMITED)
        connectable = Connectable { output ->
            object : Connection<String> {
                override fun accept(value: String) {
                    if (value == "crash") {
                        throw RuntimeException("crashing!")
                    }

                    output.accept(value.length)
                }

                override fun dispose() = Unit
            }
        }
    }

    @Test
    fun shouldEmitTransformedItems() = runTest {
        val results = async {
            input.consumeAsFlow()
                .transform(connectable)
                .toList()
        }

        input.send(".")
        input.send("..")
        input.send("...")
        input.close()

        assertEquals("1, 2, 3", results.await().joinToString())
    }

    @Test
    fun shouldPropagateCompletion() = runTest {
        withTimeout(1000) {
            input.consumeAsFlow()
                .transform(connectable)
                .onStart {
                    input.send("hi")
                    input.close()
                }
                .collect {}
        }

        assertTrue(input.isClosedForReceive)
        assertTrue(input.isClosedForSend)
    }

    @Test
    fun shouldPropagateErrorsFromConnectable() = runTest {
        val output = async {
            runCatching {
                input.consumeAsFlow()
                    .transform(connectable)
                    .toList()
            }
        }

        input.send("crash")

        try {
            output.await().onFailure { throw it }
            fail()
        } catch (e: Exception) {
            assertEquals("crashing!", e.message)
        }
    }

    @Test
    fun shouldPropagateErrorsFromUpstream() = runTest {
        val output = async {
            runCatching {
                input.consumeAsFlow()
                    .transform(connectable)
                    .toList()
            }
        }

        input.close(RuntimeException("expected"))

        try {
            output.await().onFailure { throw it }
            fail()
        } catch (e: Exception) {
            assertEquals("expected", e.message)
        }
    }

    @Test
    fun testFlowConnectable() = runTest {
        val connectable = flowTransformer<Int, String> { ints ->
            ints.map { it.toString() }
        }.asConnectable()

        val recordingConsumer = RecordingConsumer<String>()
        val connection = connectable.connect(recordingConsumer)

        connection.accept(1)
        connection.accept(2)
        connection.accept(3)

        connection.dispose()

        connection.accept(4)

        recordingConsumer.assertValues("1", "2", "3")
    }
}
