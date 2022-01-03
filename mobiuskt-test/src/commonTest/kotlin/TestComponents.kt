package kt.mobius.test


data class TestModel(
    val number: Int = 0,
    val string: String? = null,
)

sealed class TestEvent {
    object Noop : TestEvent()

    object DecrementNumber : TestEvent()

    data class SetString(val string: String) : TestEvent()

    object GenerateEffect1 : TestEvent()

    object GenerateEffect2 : TestEvent()

    object GenerateBothEffects : TestEvent()
}

sealed class TestEffect {
    object SideEffect1 : TestEffect()

    data class SideEffect2(val string: String) : TestEffect()
}