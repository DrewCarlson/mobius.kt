package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kt.mobius.internal_util.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * Creates a [Connectable] that delegates connection creation to [effectHandlers]
 * and the corresponding [Connection]s.
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class CompositeEffectHandler<I, O> private constructor(
    private val effectHandlers: Array<out Connectable<I, O>>
) : Connectable<I, O> {

    public companion object {
        @JvmStatic
        @JsName("from")
        public fun <I, O> from(vararg effectHandlers: Connectable<I, O>): Connectable<I, O> =
            CompositeEffectHandler(effectHandlers)

        @JvmStatic
        @JsName("fromList")
        public fun <I, O> from(effectHandlers: List<Connectable<I, O>>): Connectable<I, O> =
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
