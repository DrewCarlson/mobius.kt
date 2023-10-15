package kt.mobius.internal_util

import kotlin.test.Test
import kotlin.test.assertEquals

class StringFormatTests {

    @Test
    fun test_WithoutArgs() {
        assertEquals("Hello, World!", "Hello, World!".format())
    }

    @Test
    fun test_WithArg_AtMessageStart() {
        assertEquals("Hello, World!", "{}, World!".format("Hello"))
    }

    @Test
    fun test_WithArg_AtMessageEnd() {
        assertEquals("Hello, World!", "Hello, World{}".format("!"))
    }

    @Test
    fun test_WithArg_AtMessageCenter() {
        assertEquals("Hello, World!", "Hello{}World!".format(", "))
    }

    @Test
    fun test_WithArgs_Multiple() {
        assertEquals("Hello, World!", "{}, {}!".format("Hello", "World"))
    }

    @Test
    fun test_WithArgs_AllArgs() {
        assertEquals("Hello, World!", "{}{}{}{}{}".format("Hello", ",", " ", "World", "!"))
    }

    @Test
    fun test_WithArgs_NullArg() {
        assertEquals("Hello, null!", "Hello, {}!".format(null))
    }

    @Test
    fun test_WithArgs_BoolArg() {
        assertEquals("Hello, true!", "Hello, {}!".format(true))
    }
}
