package kt.mobius.test.matcher

internal abstract class DiagnosingMatcher<T> : BaseMatcher<T>() {
    override fun matches(actual: Any?): Boolean {
        return matches(actual, Description.NONE)
    }

    override fun describeMismatch(actual: Any?, mismatchDescription: Description) {
        matches(actual, mismatchDescription)
    }

    protected abstract fun matches(item: Any?, mismatchDescription: Description): Boolean
}
