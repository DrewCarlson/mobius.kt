package kt.mobius

import kt.mobius.Effects.effects
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirstTest {

    @Test
    fun supportsCreatingWithVarargs() {
        val f = First.first("hi", effects("effect", "äffäkt"))

        assertEquals(f.model(), "hi")
        assertTrue(f.hasEffects())
        assertTrue(f.effects().containsAll(listOf("effect", "äffäkt")))
    }

    @Test
    fun supportsCreatingWithSet() {
        val f = First.first("hi", setOf("ho", "there"))

        assertTrue(f.hasEffects())
        assertTrue(f.effects().containsAll(listOf("ho", "there")))
    }

    @Test
    fun reportsNoEffectsIfThereAreNoEffects() {
        val f = First.first<String, String>("hi")

        assertFalse(f.hasEffects())
    }

    @Test
    fun shouldHaveCorrectEqualsWithEffects() {
        val f1 = First.first("hi", effects("hello", "there"))
        val f2 = First.first("hi", effects("there", "hello"))
        val f3 = First.first("hi", setOf("hello", "there"))

        val g1 = First.first("hi", effects("hello", "there", "you"))
        val g2 = First.first("hi", setOf("hello", "there", "you"))

        val h1 = First.first<String, String>("hi")
        val h2 = First.first<String, String>("hi", setOf())
        val h3 = First.first<String, String>("hi", effects())

        val i1 = First.first("hey", effects("hello", "there"))
        val j1 = First.first("hey", effects("hello", "there", "you"))
        val k1 = First.first<String, String>("hey")
        val k2 = First.first<String, String>("hey", effects())

        assertAllEquals(f1, f2, f3)
        assertAllEquals(g1, g2)
        assertAllEquals(h1, h2, h3)
        assertAllEquals(i1)
        assertAllEquals(j1)
        assertAllEquals(k1, k2)
    }
}
