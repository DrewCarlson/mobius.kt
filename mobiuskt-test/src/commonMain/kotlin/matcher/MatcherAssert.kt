package kt.mobius.test.matcher

import kotlin.test.fail

public object MatcherAssert {
    public fun <T : Any> assertThat(actual: T?, matcher: Matcher<in T>) {
        assertThat("", actual, matcher)
    }

    public fun <T : Any> assertThat(reason: String, actual: T?, matcher: Matcher<in T>) {
        if (!matcher.matches(actual)) {
            val description: Description = StringDescription()
            description.appendText(reason)
                .appendText("\n")
                .appendText("Expected: ")
                .appendDescriptionOf(matcher)
                .appendText("\n")
                .appendText("     but: ")
            matcher.describeMismatch(actual, description)
            fail(description.toString())
        }
    }
}