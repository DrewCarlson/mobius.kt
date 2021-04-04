package kt.mobius

import kt.mobius.Effects.effects
import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NextTest {

    @Test
    fun shouldNotBeSensitiveToExternalMutation() {
        val inputs = hashSetOf("in")

        val next = next("#", inputs)

        inputs.add("don't want to see this one")

        assertEquals(next.effects(), hashSetOf("in"))
    }

    @Test
    fun shouldNotCareAboutEffectOrder() {
        val original = next("model", effects("e1", "e2"))
        val reordered = next("model", effects("e2", "e1"))

        assertEquals(original, reordered)
    }

    @Test
    fun nextNoopHasNoModelAndNoEffects() {
        val next = noChange<Any, Any>()

        assertFalse(next.hasModel())
        assertFalse(next.hasEffects())
    }

    @Test
    fun nextEffectsOnlyHasEffects() {
        val next = dispatch<String, String>(effects("foo"))

        assertFalse(next.hasModel())
        assertTrue(next.hasEffects())
    }

    @Test
    fun nextNoEffectsOnlyHasModel() {
        val next = Next.next<String, String>("foo")

        assertTrue(next.hasModel())
        assertFalse(next.hasEffects())
    }

    @Test
    fun nextModelAndEffectsHasBothModelAndEffects() {
        val next = next("m", effects("f"))

        assertTrue(next.hasModel())
        assertTrue(next.hasEffects())
    }

    @Test
    fun andEffectsFactoriesAreEquivalent() {
        val a = Next.next("m", effects("f1", "f2", "f3"))
        val b = Next.next("m", setOf("f1", "f2", "f3"))

        assertEquals(a, b)
    }

    @Test
    fun canMergeInnerEffects() {
        val outerNext = Next.next("m", effects("f1", "f2"))
        val innerNext = dispatch<String, String>(effects("f2", "f3"))

        val merged = Next.next(
            outerNext.modelOrElse("fail"), innerNext.effects() + outerNext.effects()
        )

        assertEquals(Next.next("m", effects("f1", "f2", "f3")), merged)
    }

    @Test
    fun canMergeInnerEffectsAndModel() {
        val effects = setOf("f1", "f2")
        val innerNext = Next.next(1, effects("f2", "f3"))

        val merged = Next.next("m" + innerNext.modelOrElse(0), effects + innerNext.effects())

        assertEquals(Next.next("m1", effects("f1", "f2", "f3")), merged)
    }

    @Test
    fun testEquals() {
        val m1 = next("hi", emptySet<String>())
        val m2 = next<String, String>("hi")
        val m3 = next("hi", emptySet<String>())

        val n1 = next("hi", setOf("a", "b"))
        val n2 = next("hi", effects("a", "b"))
        val n3 = next("hi", effects("b", "a"))
        val n4 = next("hi", setOf("b", "a"))

        val o1 = next("hi", setOf("a", "b", "c"))
        val o2 = next("hi", effects("a", "c", "b"))
        val o3 = next("hi", effects("b", "a", "c"))
        val o4 = next("hi", setOf("c", "b", "a"))

        val p1 = next(null, setOf("a", "b", "c"))
        val p2 = dispatch<String, String>(effects("a", "c", "b"))
        val p3 = dispatch<String, String>(effects("b", "a", "c"))
        val p4 = dispatch<String, String>(setOf("c", "b", "a"))

        val q1 = next("hey", setOf<String>())
        val q2 = next<String, String>("hey")
        val q3 = next("hey", emptySet<String>())

        val r1 = next("hey", setOf("a", "b"))
        val r2 = next("hey", effects("a", "b"))

        val s1 = next("hey", setOf("a", "b", "c"))
        val s2 = next("hey", effects("a", "b", "c"))

        assertAllEquals(m1, m2, m3)
        assertAllEquals(n1, n2, n3, n4)
        assertAllEquals(o1, o2, o3, o4)
        assertAllEquals(p1, p2, p3, p4)
        assertAllEquals(q1, q2, q3)
        assertAllEquals(r1, r2)
        assertAllEquals(s1, s2)
    }
}
