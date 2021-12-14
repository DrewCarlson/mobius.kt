package kt.mobius.testdomain

class SafeEffect(val id: String) : TestEffect {
    override fun toString(): String = "effect$id"
}