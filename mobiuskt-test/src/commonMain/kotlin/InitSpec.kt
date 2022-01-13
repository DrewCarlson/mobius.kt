package kt.mobius.test

import kt.mobius.First
import kt.mobius.Init
import kt.mobius.test.matcher.Matcher
import kt.mobius.test.matcher.MatcherAssert.assertThat
import kotlin.test.fail


/**
 * A class to help with Behavior Driven Testing of the [Init] function of a Mobius program.
 *
 * @param [M] model type
 * @param [F] effects type
 */
public class InitSpec<M, F>(
    private val init: Init<M, F>
) {

    public fun whenInit(model: M): Then<M, F> {
        return object : Then<M, F> {
            override fun then(assertion: Assert<M, F>) {
                assertion.assertFirst(init.init(model))
            }

            override fun thenError(assertion: AssertError) {
                var error: Exception? = null
                try {
                    init.init(model)
                } catch (e: Exception) {
                    error = e
                }
                if (error == null) {
                    fail("An exception was expected but was not thrown")
                }
                assertion.assertError(error)
            }
        }
    }

    /**
     * The final step in a behavior test. Instances of this class will call your function under test
     * with the previously provided values (i.e. given and when) and will pass the result of the
     * function over to your [Assert] implementation. If you choose to call [thenError],
     * your function under test will be invoked and any exceptions thrown will be caught and passed on
     * to your [AssertError] implementation. If no exceptions are thrown by the function under
     * test, then an [AssertError] will be thrown to fail the test.
     */
    public interface Then<M, F> {
        /**
         * Runs the specified test and then runs the [Assert] on the resulting [First].
         *
         * @param assertion to compare the result with
         */
        public fun then(assertion: Assert<M, F>)

        /**
         * Runs the specified test and validates that it throws the exception expected by the supplied
         * [AssertError].
         *
         * @param assertion an expectation on the exception
         */
        public fun thenError(assertion: AssertError)
    }

    /** Interface for defining your error assertions.  */
    public interface AssertError {
        public fun assertError(e: Exception)
    }

    /** Interface for defining your assertions over [First] instances.  */
    public fun interface Assert<M, F> {
        public fun assertFirst(first: First<M, F>)
    }

    public companion object {
        /**
         * Convenience function for creating assertions.
         *
         * @param matchers an array of matchers, all of which must match
         * @param [M] the model type
         * @param [F] the effect type
         * @return an [Assert] that applies all the matchers
         */
        public fun <M, F> assertThatFirst(vararg matchers: Matcher<First<M, F>>): Assert<M, F> {
            return Assert { first: First<M, F>? ->
                for (matcher in matchers) {
                    assertThat(first, matcher)
                }
            }
        }
    }
}
