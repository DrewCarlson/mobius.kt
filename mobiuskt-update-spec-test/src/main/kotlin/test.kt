package demo

import kt.mobius.*
import kt.mobius.Next.Companion.next
import kt.mobius.gen.*

@UpdateSpec(
    eventClass = TestEvent::class,
    effectClass = TestEffect::class,
)
data class TestModel(
    val counter: Int,
)

sealed class TestEvent {
    object Increment : TestEvent()
    object Decrement : TestEvent()
    data class SetValue(val newCounter: Int) : TestEvent()
}

sealed class TestEffect

@Suppress("unused")
object TestUpdate : TestUpdateSpec {
    override fun increment(model: TestModel): Next<TestModel, TestEffect> {
        return next(model.copy(counter = model.counter + 1))
    }

    override fun decrement(model: TestModel): Next<TestModel, TestEffect> {
        return next(model.copy(counter = model.counter - 1))
    }

    override fun setValue(model: TestModel, event: TestEvent.SetValue): Next<TestModel, TestEffect> {
        return next(model.copy(counter = event.newCounter))
    }
}
