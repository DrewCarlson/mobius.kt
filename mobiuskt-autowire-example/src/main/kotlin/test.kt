package demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map
import kt.mobius.*
import kt.mobius.Next.Companion.next
import kt.mobius.autowire.AutoWireEffect
import kt.mobius.autowire.AutoWireEvent
import kt.mobius.autowire.AutoWireInject
import kt.mobius.flow.FlowMobius

data class TestModel(
    val counter: Int,
)

@AutoWireEvent
internal fun onIncrement(model: TestModel): Next<TestModel, TestEffect> {
    return when (model.counter) {
        Int.MAX_VALUE -> next(model.copy(counter = Int.MIN_VALUE))
        else -> next(model.copy(counter = model.counter + 1))
    }
}

@AutoWireEvent
internal fun onDecrement(model: TestModel): Next<TestModel, TestEffect> {
    return next(model.copy(counter = model.counter - 1))
}

@AutoWireEvent
internal fun onSetValue(model: TestModel, newCounter: Int): Next<TestModel, TestEffect> {
    return next(model.copy(counter = newCounter))
}

@AutoWireEffect
internal fun saveCounter(counter: Int) {

}

@AutoWireEffect
internal suspend fun testFunc2(counter: Int): TestEvent {
    TODO()
}

@AutoWireEffect(latest = true)
internal suspend fun FlowCollector<TestEvent>.testFunc3(counter: Int) {
    emit(TestEvent.OnSetValue(counter))
    TODO()
}

@AutoWireEffect
internal fun Flow<TestEffect.TestFunc4>.testFunc4(): Flow<TestEvent> {
    return map { TestEvent.OnIncrement }
}

@AutoWireEffect
internal fun testFunc5() {

}

@AutoWireEffect
internal fun testFunc6(@AutoWireInject database: Database, id: Long) {

}

class Database {

}


fun main() {
    val loopFactory = FlowMobius.loop(TestUpdate(), TestEffectHandler(Database()))
    val loop = loopFactory.startFrom(TestModel(0))
    loop.observe(::println)
    loop.dispatchEvent(TestEvent.OnIncrement)
    loop.dispatchEvent(TestEvent.OnSetValue(3))
    loop.dispatchEvent(TestEvent.OnDecrement)
}