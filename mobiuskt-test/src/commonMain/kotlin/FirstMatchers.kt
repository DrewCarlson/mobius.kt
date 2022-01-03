package kt.mobius.test

import kt.mobius.First
import kt.mobius.test.matcher.Description
import kt.mobius.test.matcher.IsEqual.Companion.equalTo
import kt.mobius.test.matcher.IsIterableContaining.Companion.hasItems
import kt.mobius.test.matcher.Matcher
import kt.mobius.test.matcher.TypeSafeDiagnosingMatcher
import kotlin.reflect.typeOf


/** Provides utility functions for matching against [First] instances.  */
public object FirstMatchers {
    /**
     * Returns a matcher that matches [First] instances with a model that is equal to the
     * supplied one.
     *
     * @param expected the expected model
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasModel(expected: M): Matcher<First<M, F>> {
        return hasModel(equalTo(expected))
    }

    /**
     * Returns a matcher that matches [First] instances with a model that matches the supplied
     * model matcher.
     *
     * @param matcher the matcher to apply to the model
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasModel(matcher: Matcher<M>): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>>(typeOf<First<M, F>>()) {
            override fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (!matcher.matches(item.model())) {
                    mismatchDescription.appendText("bad model: ")
                    matcher.describeMismatch(item.model(), mismatchDescription)
                    false
                } else {
                    mismatchDescription.appendText("has model: ")
                    matcher.describeMismatch(item.model(), mismatchDescription)
                    true
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("has a model: ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches [First] instances with no effects.
     *
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasNoEffects(): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>>(typeOf<First<M, F>>()) {
            override fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (item.hasEffects()) {
                    mismatchDescription.appendText("has effects")
                    false
                } else {
                    mismatchDescription.appendText("no effects")
                    true
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("should have no effects")
            }
        }
    }

    /**
     * Returns a matcher that matches [First] instances whose effects match the supplied effect
     * matcher.
     *
     * @param matcher the matcher to apply to the effects
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasEffects(matcher: Matcher<Iterable<F>>): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>>(typeOf<First<M, F>>()) {
            override fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (!item.hasEffects()) {
                    mismatchDescription.appendText("no effects")
                    false
                } else if (!matcher.matches(item.effects())) {
                    mismatchDescription.appendText("bad effects: ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    false
                } else {
                    mismatchDescription.appendText("has effects: ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    true
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("has effects: ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches if all the supplied effects are present in the supplied [First], in any order.
     * The [First] may have more effects than the ones included.
     *
     * @param effects the effects to match (possibly empty)
     * @param [M] the model type
     * @param [F] the effect type
     * @return a matcher that matches [First] instances that include all the supplied effects
     */
    public fun <M, F> hasEffects(vararg effects: F): Matcher<First<M, F>> {
        return hasEffects(hasItems(*effects))
    }
}