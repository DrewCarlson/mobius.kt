package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterEffectHandlerTest {

    interface TestTrait1

    sealed class TestType {
        object A : TestType(), TestTrait1
        object B : TestType()
    }

    @Test
    fun testIgnoresExcludedClass() {
        var disposed = false
        val handler = Connectable<TestType, String> {
            object : Connection<TestType> {
                override fun accept(value: TestType) {
                    assertEquals(TestType.B, value)
                }

                override fun dispose() {
                    disposed = true
                }
            }
        }.exclude(listOf(TestTrait1::class))

        handler.connect { }.apply {
            accept(TestType.A)
            accept(TestType.B)
            dispose()
        }
        assertTrue(disposed)
    }


    @Test
    fun testIgnoresFilteredEffect() {
        var disposed = false
        val handler = Connectable<TestType, String> {
            object : Connection<TestType> {
                override fun accept(value: TestType) {
                    assertEquals(TestType.B, value)
                }

                override fun dispose() {
                    disposed = true
                }
            }
        }.filter { it !is TestType.A }

        handler.connect { }.apply {
            accept(TestType.A)
            accept(TestType.B)
            dispose()
        }
        assertTrue(disposed)
    }
}