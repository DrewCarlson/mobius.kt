package kt.mobius.android

import kt.mobius.*
import kt.mobius.functions.*

internal class TestEvent(val name: String)
internal class TestEffect(val name: String)
internal class TestModel(val name: String)
internal class TestViewEffect(val name: String)
internal class TestViewEffectHandler<E, F, V>(
    val viewEffectConsumer: Consumer<V>
) : Connectable<F, E> {
    @Volatile
    private var eventConsumer: Consumer<E>? = null


    fun sendEvent(event: E) {
        eventConsumer?.accept(event)
    }

    override fun connect(output: Consumer<E>): Connection<F> {
        if (eventConsumer != null) {
            throw ConnectionLimitExceededException()
        }
        eventConsumer = output
        return object : Connection<F> {
            override fun accept(value: F) = Unit

            override fun dispose() = Unit
        }
    }
}

/** An Effect Handler that simply sends a TestViewEffect to the given view effect consumer.  */
internal class ViewEffectSendingEffectHandler(
    private val viewEffectConsumer: Consumer<TestViewEffect>
) : Connectable<TestEffect, TestEvent> {

    override fun connect(output: Consumer<TestEvent>): Connection<TestEffect> {
        return object : Connection<TestEffect> {
            override fun accept(value: TestEffect) {
                viewEffectConsumer.accept(TestViewEffect(value.name))
            }

            override fun dispose() = Unit
        }
    }
}
