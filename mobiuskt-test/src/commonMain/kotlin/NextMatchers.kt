package kt.mobius.test

import kt.mobius.Next
import kt.mobius.test.matcher.AllOf.Companion.allOf
import kt.mobius.test.matcher.Description
import kt.mobius.test.matcher.IsEqual.Companion.equalTo
import kt.mobius.test.matcher.IsIterableContaining.Companion.hasItems
import kt.mobius.test.matcher.Matcher
import kt.mobius.test.matcher.TypeSafeDiagnosingMatcher
import kotlin.reflect.typeOf

/** Provides utility functions for matching [Next] instances in tests.  */
public object NextMatchers {
    /**
     * Returns a matcher that matches [Next] instances without a model.
     *
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasNoModel(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>>(typeOf<Next<M, F>>()) {
            override fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (item.hasModel()) {
                    mismatchDescription.appendText("it had a model: " + item.modelUnsafe())
                    return false
                }
                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Next without model")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with a model.
     *
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasModel(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>>(typeOf<Next<M, F>>()) {
            override fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (!item.hasModel()) {
                    mismatchDescription.appendText("it had no model")
                    return false
                }
                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Next with any model")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with a model that is equal to the
     * supplied one.
     *
     * @param expected the expected model
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasModel(expected: M): Matcher<Next<M, F>> {
        return hasModel(equalTo(expected))
    }

    /**
     * Returns a matcher that matches [Next] instances with a model that matches the supplied
     * model matcher.
     *
     * @param matcher the matcher to apply to the model
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasModel(matcher: Matcher<M>): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>>(typeOf<Next<M, F>>()) {
            override fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (!item.hasModel()) {
                    mismatchDescription.appendText("it had no model")
                    return false
                }
                if (!matcher.matches(item.modelUnsafe())) {
                    mismatchDescription.appendText("the model ")
                    matcher.describeMismatch(item.modelUnsafe(), mismatchDescription)
                    return false
                }
                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Next with model ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with no effects.
     *
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasNoEffects(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>>(typeOf<Next<M, F>>()) {
            override fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (item.hasEffects()) {
                    mismatchDescription.appendText("it had effects: " + item.effects())
                    return false
                }
                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Next without effects")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances whose effects match the supplied effect
     * matcher.
     *
     * @param matcher the matcher to apply to the effects
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasEffects(matcher: Matcher<Iterable<F>>): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>>(typeOf<Next<M, F>>()) {
            override fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (!item.hasEffects()) {
                    mismatchDescription.appendText("it had no effects")
                    return false
                }
                if (!matcher.matches(item.effects())) {
                    mismatchDescription.appendText("the effects were ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    return false
                }
                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Next with effects ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches if all the supplied effects are present in the supplied [Next], in any order.
     * The [Next] may have more effects than the ones included.
     *
     * @param effects the effects to match (possibly empty)
     * @param [M] the model type
     * @param [F] the effect type
     * @return a matcher that matches [Next] instances that include all the supplied effects
     */
    public fun <M, F> hasEffects(vararg effects: F): Matcher<Next<M, F>> {
        return hasEffects(hasItems(*effects))
    }

    /**
     * Returns a matcher that matches [Next] instances with no model and no effects.
     *
     * @param [M] the model type
     * @param [F] the effect type
     */
    public fun <M, F> hasNothing(): Matcher<Next<M, F>> {
        return allOf(hasNoModel(), hasNoEffects())
    }
}