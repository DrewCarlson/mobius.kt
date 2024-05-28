package kt.mobius.kotest

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull


public fun main() {
    update
        .given(TestModel())
        .whenEvent(
            TestEvent.IncrementNumber,
            TestEvent.UpdateModelAndEffect
        )
        .shouldContainEffects(setOf(TestEffect.SideEffect1))
        .shouldHaveModel { model ->
            model.number.shouldBeEqual(1)
            model.string
                .shouldNotBeNull()
                .shouldBeEqual("updated")
        }
}
