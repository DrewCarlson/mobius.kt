package kt.mobius.flow

import kt.mobius.Connection
import kt.mobius.Mobius
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kt.mobius.Next
import kotlin.test.*

class FlowMobiusLoopTest {

    private lateinit var loop: FlowTransformer<Int, String>

    @BeforeTest
    fun setUp() {
        val factory = Mobius.loop<String, Int, Boolean>(
            { model, event -> Next.next(model + event.toString()) },
            {
                object : Connection<Boolean> {
                    override fun accept(value: Boolean) = Unit
                    override fun dispose() = Unit
                }
            }
        )

        loop = FlowMobius.loopFrom(factory, "")
    }

    @Test
    fun shouldPropagateIncomingErrorsAsUnrecoverable() = runTest {
        val input = Channel<Int>()

        val result = async {
            runCatching {
                loop(input.consumeAsFlow()).toList()
            }
        }

        input.close(RuntimeException("expected"))

        try {
            result.await().onFailure { throw it }
            fail()
        } catch (e: Exception) {
            assertTrue(e is UnrecoverableIncomingException)
            assertEquals("expected", e.cause?.message)
        }
    }
}
