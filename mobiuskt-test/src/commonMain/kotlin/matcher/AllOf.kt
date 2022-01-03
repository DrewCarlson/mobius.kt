package kt.mobius.test.matcher

internal class AllOf<T>(private val matchers: Iterable<Matcher<in T>>) : DiagnosingMatcher<T>() {
    constructor(vararg matchers: Matcher<in T>) : this(matchers.asList())

    override fun matches(item: Any?, mismatchDescription: Description): Boolean {
        for (matcher in matchers) {
            if (!matcher.matches(item)) {
                mismatchDescription.appendDescriptionOf(matcher).appendText(" ")
                matcher.describeMismatch(item, mismatchDescription)
                return false
            }
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendList("(", " " + "and" + " ", ")", matchers)
    }

    companion object {
        fun <T> allOf(matchers: Iterable<Matcher<in T>>): Matcher<T> {
            return AllOf(matchers)
        }

        fun <T> allOf(vararg matchers: Matcher<in T>): Matcher<T> {
            return allOf(matchers.asList())
        }
    }
}