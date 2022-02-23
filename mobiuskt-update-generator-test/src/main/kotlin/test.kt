package demo

import kt.mobius.*
import kt.mobius.Next.Companion.next
import kt.mobius.functions.Consumer
import kt.mobius.gen.*

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
@GenerateUpdate
object TestUpdate : Update<TestModel, TestEvent, TestEffect>, TestGeneratedUpdate {
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

fun createHandler(consumer: Consumer<TestEvent>) = object : Connection<TestEffect> {
    override fun accept(value: TestEffect) = Unit
    override fun dispose() = Unit
}

fun main() {
    val loopFactory = Mobius.loop(TestUpdate, ::createHandler)
    val loop = loopFactory.startFrom(TestModel(0))
    loop.observe(::println)
    loop.dispatchEvent(TestEvent.Increment)
    loop.dispatchEvent(TestEvent.SetValue(3))
    loop.dispatchEvent(TestEvent.Decrement)
}
