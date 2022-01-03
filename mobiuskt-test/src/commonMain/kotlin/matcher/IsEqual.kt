package kt.mobius.test.matcher

public class IsEqual<T>(
    private val expectedValue: T
) : BaseMatcher<T>() {

    override fun matches(actual: Any?): Boolean {
        return areEqual(actual, expectedValue)
    }

    override fun describeTo(description: Description) {
        description.appendValue(expectedValue)
    }

    public companion object {
        private fun areEqual(actual: Any?, expected: Any?): Boolean {
            if (actual == null) {
                return expected == null
            }
            return if (expected != null && isArray(actual)) {
                isArray(expected) && areArraysEqual(actual, expected)
            } else actual == expected
        }


        private fun areArraysEqual(actualArray: Any, expectedArray: Any): Boolean {
            return areArrayLengthsEqual(actualArray, expectedArray) && areArrayElementsEqual(actualArray, expectedArray)
        }

        private fun areArrayLengthsEqual(actualArray: Any, expectedArray: Any): Boolean {
            return (actualArray as Array<*>).size == (expectedArray as Array<*>).size
        }

        private fun areArrayElementsEqual(actualArray: Any, expectedArray: Any): Boolean {
            val aArray = (actualArray as Array<*>)
            (expectedArray as Array<*>)
            aArray.forEachIndexed { index, actual ->
                if (!areEqual(actual, expectedArray[index])) {
                    return false
                }
            }
            return true
        }

        private fun isArray(o: Any): Boolean {
            return o is Array<*>
        }

        public fun <T> equalTo(operand: T): Matcher<T> {
            return IsEqual(operand)
        }
    }
}