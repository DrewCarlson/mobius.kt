package kt.mobius.test

import kt.mobius.First
import kt.mobius.Init
import kt.mobius.test.FirstMatchers.hasEffects
import kt.mobius.test.FirstMatchers.hasModel
import kt.mobius.test.FirstMatchers.hasNoEffects
import kt.mobius.test.InitSpec.Companion.assertThatFirst
import kt.mobius.test.matcher.descriptionOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InitSpecAndFirstMatcherTests {

    private val initWithEffects = Init<TestModel, TestEffect> { first ->
        First.first(first, setOf(TestEffect.SideEffect2("Hello World!")))
    }

    private val initWithoutEffects = Init<TestModel, TestEffect> { first ->
        First.first(first)
    }

    private val initWithNewModel = Init<TestModel, TestEffect> {
        First.first(TestModel(number = 10, string = "Hello World!"))
    }

    @Test
    fun testExpectsNoEffectsAndHasNoEffects() {
        val spec = InitSpec(initWithoutEffects)
        spec.whenInit(TestModel())
            .then(assertThatFirst(hasNoEffects()))
    }

    @Test
    fun testExpectsNoEffectsButHasEffects() {
        val spec = InitSpec(initWithEffects)

        val error = assertFailsWith<AssertionError> {
            spec.whenInit(TestModel())
                .then(assertThatFirst(hasNoEffects()))
        }

        assertEquals(
            "\nExpected: should have no effects\n" +
                    "     but: has effects",
            error.message
        )
    }

    @Test
    fun testExpectsEffectsAndHasEffects() {
        val spec = InitSpec(initWithEffects)
        spec.whenInit(TestModel())
            .then(assertThatFirst(hasEffects(TestEffect.SideEffect2("Hello World!"))))
    }

    @Test
    fun testExpectsIncorrectEffects() {
        val spec = InitSpec(initWithEffects)

        val error = assertFailsWith<AssertionError> {
            spec.whenInit(TestModel())
                .then(assertThatFirst(hasEffects(TestEffect.SideEffect1)))
        }

        assertEquals(
            "\nExpected: has effects: (a collection containing <${descriptionOf(TestEffect.SideEffect1)}>)\n" +
                    "     but: bad effects: a collection containing <${descriptionOf(TestEffect.SideEffect1)}> mismatches were: [was <SetString(string=Hello World!)>]",
            error.message
        )
    }

    @Test
    fun testExpectsCorrectInitialModel() {
        val spec = InitSpec(initWithEffects)
        val initialModel = TestModel()

        spec.whenInit(initialModel)
            .then(assertThatFirst(hasModel(initialModel)))
    }

    @Test
    fun testExpectsIncorrectInitialModel() {
        val spec = InitSpec(initWithNewModel)
        val initialModel = TestModel()

        val error = assertFailsWith<AssertionError> {
            spec.whenInit(initialModel)
                .then(assertThatFirst(hasModel(initialModel)))
        }

        assertEquals(
            "\nExpected: has a model: <TestModel(number=0, string=null)>\n" +
                    "     but: bad model: was <TestModel(number=10, string=Hello World!)>",
            error.message
        )
    }
}