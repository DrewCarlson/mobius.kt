package kt.mobius.test.matcher

public interface Matcher<T> : SelfDescribing {
    public fun matches(actual: Any?): Boolean

    public fun describeMismatch(actual: Any?, mismatchDescription: Description)
}
