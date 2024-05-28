package kt.mobius.kotest

import io.kotest.assertions.*


internal fun<T> countMismatch(expectedCounts: Map<T, Int>, actualCounts: Map<T, Int>) =
    actualCounts.entries.mapNotNull { actualEntry ->
        expectedCounts[actualEntry.key]?.let { expectedValue ->
            if(actualEntry.value != expectedValue)
                CountMismatch(actualEntry.key, expectedValue, actualEntry.value)
            else null
        }
    }

internal data class CountMismatch<T>(val key: T, val expectedCount: Int, val actualCount: Int) {
    init {
        require(expectedCount >= 0 && actualCount >= 0) {
            "Both expected and actual count should be non-negative, but expected was: $expectedCount and actual: was: $actualCount"
        }
    }

    override fun toString(): String = "Key=\"${key}\", expected count: $expectedCount, but was: $actualCount"
}



internal inline fun <T> assertSoftlyWithMessage(
    message: String,
    assertions: () -> T
): T {
    // Handle the edge case of nested calls to this function by only calling throwCollectedErrors in the
    // outermost verifyAll block
    if (errorCollector.getCollectionMode() == ErrorCollectionMode.Soft) {
        val oldErrors = errorCollector.errors()
        errorCollector.clear()
        errorCollector.depth++

        return try {
            assertions()
        } finally {
            val aggregated = errorCollector.collectiveError()
            errorCollector.clear()
            errorCollector.pushErrors(oldErrors)
            aggregated?.let { errorCollector.pushError(it) }
            errorCollector.depth--
        }
    }

    errorCollector.setCollectionMode(ErrorCollectionMode.Soft)
    return try {
        assertions()
    } finally {
        // In case if any exception is thrown from assertions block setting errorCollectionMode back to hard
        // so that it won't remain soft for others tests. See https://github.com/kotest/kotest/issues/1932
        errorCollector.setCollectionMode(ErrorCollectionMode.Hard)
        errorCollector.collectiveError()?.let { error ->
            throw AssertionError(
                buildString {
                    appendLine(message)
                    appendLine(error.message)
                }
            )
        }
    }
}