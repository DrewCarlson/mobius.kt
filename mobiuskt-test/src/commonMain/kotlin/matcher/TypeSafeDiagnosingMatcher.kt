package kt.mobius.test.matcher

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.cast

public abstract class TypeSafeDiagnosingMatcher<T : Any> private constructor(
    private val expectedType: KClass<T>
) : BaseMatcher<T>() {

    @Suppress("UNCHECKED_CAST")
    public constructor(kType: KType) : this(kType.classifier as KClass<T>)

    protected abstract fun matchesSafely(item: T, mismatchDescription: Description): Boolean

    override fun matches(actual: Any?): Boolean {
        return (actual != null && expectedType.isInstance(actual)
                && matchesSafely(expectedType.cast(actual), Description.NONE))
    }

    override fun describeMismatch(actual: Any?, mismatchDescription: Description) {
        if (actual == null) {
            mismatchDescription.appendText("was null")
        } else if (!expectedType.isInstance(actual)) {
            mismatchDescription.appendText("was ")
                .appendText(actual::class.simpleName ?: "<unknown>")
                .appendText(" ")
                .appendValue(actual)
        } else {
            @Suppress("UNCHECKED_CAST")
            matchesSafely(actual as T, mismatchDescription)
        }
    }
}