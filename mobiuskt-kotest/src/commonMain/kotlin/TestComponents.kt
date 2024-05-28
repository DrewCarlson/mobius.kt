package kt.mobius.kotest

import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update


public data class TestModel(
    val number: Int = 0,
    val string: String? = null,
)

public sealed class TestEvent {
    public object Noop : TestEvent()

    public object IncrementNumber : TestEvent()

    public object DecrementNumber : TestEvent()

    public data class SetString(val string: String) : TestEvent()

    public object UpdateModelAndEffect : TestEvent()

    public object GenerateEffect1 : TestEvent()

    public object GenerateEffect2 : TestEvent()

    public object GenerateBothEffects : TestEvent()
}

public sealed class TestEffect {
    public object SideEffect1 : TestEffect()

    public data class SideEffect2(val string: String) : TestEffect()
}


public val update: Update<TestModel, TestEvent, TestEffect> =
    Update { model, event ->
        when (event) {
            is TestEvent.SetString -> next(model.copy(string = event.string))
            TestEvent.IncrementNumber -> next(model.copy(number = model.number + 1))
            TestEvent.DecrementNumber -> next(model.copy(number = model.number - 1))
            TestEvent.Noop -> noChange()
            TestEvent.GenerateBothEffects ->
                dispatch(setOf(TestEffect.SideEffect1, TestEffect.SideEffect2("Hello World!")))

            TestEvent.GenerateEffect1 -> dispatch(setOf(TestEffect.SideEffect1))
            TestEvent.GenerateEffect2 -> dispatch(setOf(TestEffect.SideEffect2("Hello World!")))
            TestEvent.UpdateModelAndEffect -> next(model.copy(string = "updated"), setOf(TestEffect.SideEffect1))
        }
    }