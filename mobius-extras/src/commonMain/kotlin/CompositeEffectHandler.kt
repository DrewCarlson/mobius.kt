package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * Creates a [Connectable] that delegates connection creation to [effectHandlers]
 * and the corresponding [Connection]s.
 */
class CompositeEffectHandler<I, O> private constructor(
    private val effectHandlers: Array<out Connectable<I, O>>
) : Connectable<I, O> {

    companion object {
        @JvmStatic
        @JsName("from")
        fun <I, O> from(vararg effectHandlers: Connectable<I, O>): Connectable<I, O> =
            CompositeEffectHandler(effectHandlers)

        @JvmStatic
        @JsName("fromList")
        fun <I, O> from(effectHandlers: List<Connectable<I, O>>): Connectable<I, O> =
            CompositeEffectHandler(effectHandlers.toTypedArray())
    }

    override fun connect(output: Consumer<O>): Connection<I> {
        val consumers = effectHandlers.map { it.connect(output) }

        return object : Connection<I> {
            override fun accept(value: I) =
                consumers.forEach { it.accept(value) }

            override fun dispose() =
                consumers.forEach { it.dispose() }
        }
    }
}
