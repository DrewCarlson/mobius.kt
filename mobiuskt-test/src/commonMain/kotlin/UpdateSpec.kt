package kt.mobius.test

import kotlinx.collections.immutable.persistentListOf
import kt.mobius.Next
import kt.mobius.Update
import kt.mobius.test.matcher.Matcher
import kt.mobius.test.matcher.MatcherAssert.assertThat
import kotlin.test.fail

/**
 * A class to help with Behavior Driven Testing of the [Update] function of a Mobius program.
 *
 * @param [M] model type
 * @param [E] events type
 * @param [F] effects type
 */
public class UpdateSpec<M, E, F>(
    private val update: Update<M, E, F>
) {

    public fun given(model: M): When {
        return When(model)
    }

    public inner class When internal constructor(
        private val model: M
    ) {

        /**
         * Defines the event(s) that should be executed when the test is run. Events are executed in the
         * order supplied.
         *
         * @param event the first events
         * @param events the following events, possibly none
         * @return a [Then] instance for the remainder of the spec
         */
        public fun `when`(event: E, vararg events: E): Then<M, F> {
            return ThenImpl(model, event, *events)
        }

        /**
         * Defines the event that should be executed when the test is run. Events are executed in the
         * order supplied. This method is just an alias to [.when] for use with Kotlin
         *
         * @param event the first events
         * @return a [Then] instance for the remainder of the spec
         */
        public fun whenEvent(event: E): Then<M, F> {
            return `when`(event)
        }

        /**
         * Defines the event(s) that should be executed when the test is run. Events are executed in the
         * order supplied. This method is just an alias to [when] for use with Kotlin
         *
         * @param event the first events
         * @param events the following events, possibly none
         * @return a [Then] instance for the remainder of the spec
         */
        public fun whenEvents(event: E, vararg events: E): Then<M, F> {
            return `when`(event, *events)
        }
    }

    /**
     * The final step in a behavior test. Instances of this class will call your function under test
     * with the previously provided values (i.e. given and when) and will pass the result of the
     * function over to your [Assert] implementation. If you choose to call `thenError`,
     * your function under test will be invoked and any exceptions thrown will be caught and passed on
     * to your [AssertionError] implementation. If no exceptions are thrown by the function
     * under test, then an [AssertionError] will be thrown to fail the test.
     */
    public interface Then<M, F> {
        /**
         * Runs the specified test and then invokes the [Assert] on the [Result].
         *
         * @param assertion to compare the result with
         */
        public fun then(assertion: Assert<M, F>)

        /**
         * Runs the specified test and validates that the last step throws the exception expected by the
         * supplied [AssertError]. Note that if the test specification has multiple events, it
         * will fail if the exception is thrown before the execution of the last event.
         *
         * @param assertion an expectation on the exception
         */
        public fun thenError(assertion: AssertError)
    }

    /** Interface for defining your error assertions.  */
    public interface AssertError {
        public fun assertError(e: Exception)
    }

    /** Interface for defining your assertions over [Next] instances.  */
    public fun interface Assert<M, F> {
        public fun apply(result: Result<M, F>)
    }

    private inner class ThenImpl constructor(
        private val model: M,
        event: E,
        vararg events: E
    ) : Then<M, F> {
        private val events: MutableList<E>

        init {
            this.events = ArrayList(events.size + 1)
            this.events.add(event)
            this.events.addAll(persistentListOf(*events))
        }

        override fun then(assertion: Assert<M, F>) {
            var last: Next<M, F>? = null
            var lastModel = model
            for (event in events) {
                last = update.update(lastModel, event)
                lastModel = last.modelOrElse(lastModel)
            }
            assertion.apply(Result.of(lastModel, checkNotNull(last)))
        }

        override fun thenError(assertion: AssertError) {
            var error: Exception? = null
            var lastModel = model

            // play all events up to the last one
            for (i in 0 until events.size - 1) {
                lastModel = update.update(lastModel, events[i]).modelOrElse(lastModel)
            }

            // then, do the assertion on the final event
            try {
                update.update(model, events[events.size - 1])
            } catch (e: Exception) {
                error = e
            }
            if (error == null) {
                fail("An exception was expected but was not thrown")
            }
            assertion.assertError(error)
        }
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
        public fun <M, F> assertThatNext(vararg matchers: Matcher<Next<M, F>>): Assert<M, F> {
            return Assert { result: Result<M, F> ->
                for (matcher in matchers) {
                    assertThat(result.lastNext, matcher)
                }
            }
        }
    }
}
