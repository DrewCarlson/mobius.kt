package kt.mobius

import kotlin.test.assertEquals

fun assertAllEquals(vararg subjects: Any) {
    subjects.forEach { any ->
        subjects.forEach { any2 ->
            assertEquals(any, any2)
        }
    }
}
