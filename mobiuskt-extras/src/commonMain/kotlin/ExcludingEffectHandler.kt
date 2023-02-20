package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * Creates a [Connectable] that delegates to [effectHandler] but rejects
 * [I] values if the [exclude] function returns true.
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class ExcludingEffectHandler<I, O> constructor(
    private val effectHandler: Connectable<I, O>,
    private val exclude: (I) -> Boolean,
) : Connectable<I, O> {

    public companion object {
        @JvmStatic
        @JsName("from")
        public fun <I, O> from(
            effectHandler: Connectable<I, O>,
            vararg excludes: KClass<*>,
        ): Connectable<I, O> {
            return from(effectHandler, excludes.toList())
        }

        @JvmStatic
        @JsName("fromList")
        public fun <I, O> from(
            effectHandler: Connectable<I, O>,
            excludes: List<KClass<*>>,
        ): Connectable<I, O> =
            ExcludingEffectHandler(effectHandler) { value ->
                excludes.none { it.isInstance(value) }
            }
    }

    override fun connect(output: Consumer<O>): Connection<I> {
        val connection = effectHandler.connect(output)
        return object : Connection<I> {
            override fun accept(value: I) {
                if (exclude(value)) {
                    connection.accept(value)
                }
            }

            override fun dispose() = connection.dispose()
        }
    }
}

private fun <I, O> Connectable<I, O>.exclude(predicate: (I) -> Boolean): Connectable<I, O> =
    ExcludingEffectHandler(this, predicate)

public fun <I, O> Connectable<I, O>.exclude(vararg effects: KClass<*>): Connectable<I, O> =
    ExcludingEffectHandler.from(this, effects.toList())

public fun <I, O> Connectable<I, O>.exclude(effects: List<KClass<*>>): Connectable<I, O> =
    ExcludingEffectHandler.from(this, effects)
