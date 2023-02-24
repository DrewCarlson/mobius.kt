package demo

import kt.mobius.Next
import kt.mobius.Update
import kt.mobius.gen.DisableSubtypeSpec
import kt.mobius.gen.GenerateUpdate


data class TestWithSealedModel(
    val counter: Int,
)

sealed class TestEventWithSealed {
    data class Test1(val i: Int = 0) : TestEventWithSealed()
    object Test2 : TestEventWithSealed()
    sealed class Test3 : TestEventWithSealed() {
        data class A(val i: Int = 0) : Test3()
        object B : Test3()
        @DisableSubtypeSpec
        sealed class C : Test3() {
            data class D(val i: Int = 0) : C()
            object E : C()
        }
    }
}

@Suppress("unused")
@GenerateUpdate
object TestWithSealedUpdate : Update<TestWithSealedModel, TestEventWithSealed, TestEffect>,
    TestWithSealedGeneratedUpdate {
    override fun test2(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect> {
        TODO("Not yet implemented")
    }

    override fun test1(
        model: TestWithSealedModel,
        event: TestEventWithSealed.Test1
    ): Next<TestWithSealedModel, TestEffect> {
        TODO("Not yet implemented")
    }

    override fun test3B(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect> {
        TODO("Not yet implemented")
    }

    override fun test3A(
        model: TestWithSealedModel,
        event: TestEventWithSealed.Test3.A
    ): Next<TestWithSealedModel, TestEffect> {
        TODO("Not yet implemented")
    }

    override fun test3C(
        model: TestWithSealedModel,
        event: TestEventWithSealed.Test3.C
    ): Next<TestWithSealedModel, TestEffect> {
        TODO("Not yet implemented")
    }

}