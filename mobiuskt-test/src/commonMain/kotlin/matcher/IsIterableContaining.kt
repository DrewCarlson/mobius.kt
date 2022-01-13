package kt.mobius.test.matcher

import kt.mobius.test.matcher.AllOf.Companion.allOf
import kt.mobius.test.matcher.IsEqual.Companion.equalTo
import kotlin.reflect.typeOf

public class IsIterableContaining<T>(
    private val elementMatcher: Matcher<in T>
) : TypeSafeDiagnosingMatcher<Iterable<T>>(typeOf<Iterable<T>>()) {
    override fun matchesSafely(item: Iterable<T>, mismatchDescription: Description): Boolean {
        if (isEmpty(item)) {
            mismatchDescription.appendText("was empty")
            return false
        }
        for (element in item) {
            if (elementMatcher.matches(element)) {
                return true
            }
        }
        mismatchDescription.appendText("mismatches were: [")
        var isPastFirst = false
        for (element in item) {
            if (isPastFirst) {
                mismatchDescription.appendText(", ")
            }
            elementMatcher.describeMismatch(element, mismatchDescription)
            isPastFirst = true
        }
        mismatchDescription.appendText("]")
        return false
    }

    private fun isEmpty(iterable: Iterable<T>): Boolean {
        return !iterable.iterator().hasNext()
    }

    override fun describeTo(description: Description) {
        description
            .appendText("a collection containing ")
            .appendDescriptionOf(elementMatcher)
    }

    public companion object {
        public fun <T> hasItem(itemMatcher: Matcher<T>): Matcher<Iterable<T>> {
            return IsIterableContaining(itemMatcher)
        }

        public fun <T> hasItem(item: T): Matcher<Iterable<T>> {
            // Doesn't forward to hasItem() method so compiler can sort out generics.
            return IsIterableContaining<T>(equalTo(item))
        }

        public fun <T> hasItems(vararg itemMatchers: Matcher<T>): Matcher<Iterable<T>> {
            val all: MutableList<Matcher<in Iterable<T>>> = ArrayList(itemMatchers.size)
            for (elementMatcher in itemMatchers) {
                // Doesn't forward to hasItem() method so compiler can sort out generics.
                all.add(IsIterableContaining(elementMatcher))
            }
            return allOf(all)
        }

        public fun <T> hasItems(vararg items: T): Matcher<Iterable<T>> {
            val all: MutableList<Matcher<in Iterable<T>>> = ArrayList(items.size)
            for (item in items) {
                all.add(hasItem(item))
            }
            return allOf(all)
        }
    }
}
