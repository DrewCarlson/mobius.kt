package kt.mobius.test.matcher


public abstract class BaseMatcher<T> : Matcher<T> {

    override fun describeMismatch(actual: Any?, mismatchDescription: Description) {
        mismatchDescription.appendText("was ").appendValue(actual)
    }

    override fun toString(): String {
        return StringDescription.toString(this)
    }

    public companion object {
        protected fun isNotNull(actual: Any?, mismatch: Description): Boolean {
            if (actual == null) {
                mismatch.appendText("was null")
                return false
            }
            return true
        }
    }
}
