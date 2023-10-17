package kt.mobius.test

import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update
import kt.mobius.test.NextMatchers.hasEffects
import kt.mobius.test.NextMatchers.hasModel
import kt.mobius.test.NextMatchers.hasNoEffects
import kt.mobius.test.NextMatchers.hasNoModel
import kt.mobius.test.NextMatchers.hasNothing
import kt.mobius.test.UpdateSpec.Companion.assertThatNext
import kt.mobius.test.matcher.descriptionOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UpdateSpecAndNextMatcherTests {

    private val updateSubject = Update<TestModel, TestEvent, TestEffect> { model, event ->
        when (event) {
            is TestEvent.SetString -> next(model.copy(string = event.string))
            TestEvent.DecrementNumber -> next(model.copy(number = model.number - 1))
            TestEvent.Noop -> noChange()
            TestEvent.GenerateBothEffects ->
                dispatch(setOf(TestEffect.SideEffect1, TestEffect.SideEffect2("Hello World!")))

            TestEvent.GenerateEffect1 -> dispatch(setOf(TestEffect.SideEffect1))
            TestEvent.GenerateEffect2 -> dispatch(setOf(TestEffect.SideEffect2("Hello World!")))
        }
    }

    @Test
    fun testNextWithErrorSuccess() {
        UpdateSpec(Update<TestModel, TestEvent, TestEffect> { _, _ -> error("Expected error") })
            .given(TestModel())
            .whenEvent(TestEvent.DecrementNumber)
            .thenError { error ->
                assertIs<IllegalStateException>(error)
                assertEquals("Expected error", error.message)
            }
    }

    @Test
    fun testNextWithErrorFailed() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(Update<TestModel, TestEvent, TestEffect> { _, _ -> noChange() })
                .given(TestModel())
                .whenEvents(
                    TestEvent.DecrementNumber,
                    TestEvent.Noop
                )
                .thenError { }
        }
        assertEquals("An exception was expected but was not thrown", error.message)
    }

    @Test
    fun testNextWithModelHasModelSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.DecrementNumber)
            .then(assertThatNext(hasModel()))
    }

    @Test
    fun testNextWithModelHasExpectedModelSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.DecrementNumber)
            .then(assertThatNext(hasModel(TestModel(number = -1))))
    }

    @Test
    fun testNextWithModelHasNoEffectsSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.DecrementNumber)
            .then(assertThatNext(hasNoEffects()))
    }

    @Test
    fun testNextWithModelHasUnexpectedModelError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.DecrementNumber)
                .then(assertThatNext(hasModel(TestModel(number = -2))))
        }

        assertEquals(
            "\nExpected: Next with model <TestModel(number=-2, string=null)>\n" +
                    "     but: the model was <TestModel(number=-1, string=null)>",
            error.message
        )
    }

    @Test
    fun testNextWithModelHasNoModelErrors() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.DecrementNumber)
                .then(assertThatNext(hasNoModel()))
        }

        assertEquals(
            "\nExpected: Next without model\n" +
                    "     but: it had a model: TestModel(number=-1, string=null)",
            error.message
        )
    }


    @Test
    fun testNextWithModelHasNothingError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.DecrementNumber)
                .then(assertThatNext(hasNothing()))
        }

        assertEquals(
            "\nExpected: (Next without model and Next without effects)\n" +
                    "     but: Next without model it had a model: TestModel(number=-1, string=null)",
            error.message
        )
    }

    @Test
    fun testNextNoChangeHasNothingSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.Noop)
            .then(assertThatNext(hasNothing()))
    }

    @Test
    fun testNextNoChangeHasModelError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.Noop)
                .then(assertThatNext(hasModel()))
        }

        assertEquals(
            "\nExpected: Next with any model\n" +
                    "     but: it had no model",
            error.message
        )
    }

    @Test
    fun testNextNoChangeHasNoModelSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.Noop)
            .then(assertThatNext(hasNoModel()))
    }

    @Test
    fun testNextNoChangeHasExactModelError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.Noop)
                .then(assertThatNext(hasModel(TestModel(number = -1))))
        }

        assertEquals(
            "\nExpected: Next with model <TestModel(number=-1, string=null)>\n" +
                    "     but: it had no model",
            error.message
        )
    }

    @Test
    fun testNextNoChangeHasNoEffectsSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.Noop)
            .then(assertThatNext(hasNoEffects()))
    }

    @Test
    fun testNextNoChangeHasEffectsError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.Noop)
                .then(assertThatNext(hasEffects(TestEffect.SideEffect1)))
        }

        assertEquals(
            "\nExpected: Next with effects (a collection containing <${descriptionOf(TestEffect.SideEffect1)}>)\n" +
                    "     but: it had no effects",
            error.message
        )
    }

    @Test
    fun testNextWithEffectsHasEffectsSuccess() {
        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.GenerateEffect1)
            .then(assertThatNext(hasEffects(TestEffect.SideEffect1)))

        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.GenerateEffect2)
            .then(assertThatNext(hasEffects(TestEffect.SideEffect2("Hello World!"))))

        UpdateSpec(updateSubject)
            .given(TestModel())
            .whenEvent(TestEvent.GenerateBothEffects)
            .then(
                assertThatNext(
                    hasEffects(
                        TestEffect.SideEffect2("Hello World!"),
                        TestEffect.SideEffect1
                    )
                )
            )
    }

    @Test
    fun testNextWithEffect1HasEffect2Error() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.GenerateEffect1)
                .then(assertThatNext(hasEffects(TestEffect.SideEffect2("Hello World!"))))
        }

        assertEquals(
            "\nExpected: Next with effects (a collection containing <SideEffect2(string=Hello World!)>)\n" +
                    "     but: the effects were a collection containing <SideEffect2(string=Hello World!)> mismatches were:" +
                    " [was <${descriptionOf(TestEffect.SideEffect1)}>]",
            error.message
        )
    }

    @Test
    fun testNextWithEffectHasNothingError() {
        val error = assertFailsWith<AssertionError> {
            UpdateSpec(updateSubject)
                .given(TestModel())
                .whenEvent(TestEvent.GenerateEffect1)
                .then(assertThatNext(hasNothing()))
        }

        assertEquals(
            "\nExpected: (Next without model and Next without effects)\n" +
                    "     but: Next without effects it had effects: [${descriptionOf(TestEffect.SideEffect1)}]",
            error.message
        )
    }
}