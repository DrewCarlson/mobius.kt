package kt.mobius

import kt.mobius.internal_util.Throwables
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ThrowablesTest {

    @Test
    fun testPropagateWrapsWithRuntimeException() {
        assertFailsWith<RuntimeException> {
            Throwables.propagate(IllegalStateException(""))
        }
    }

    @Test
    fun testRethrowsRuntimeException() {
        val error = assertFailsWith<RuntimeException> {
            Throwables.propagate(RuntimeException("test"))
        }
        assertEquals("test", error.message)
    }
}