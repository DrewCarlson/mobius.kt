package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kt.mobius.internal_util.JsExport


/**
 * Creates a [Connectable] that delegates to [effectHandler] and maps the Effect
 * and Events using [mapEffect] and [mapEvent].
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class MappedEffectHandler<I, O, II, OO> constructor(
    private val effectHandler: Connectable<II, OO>,
    private val mapEffect: (I) -> II? = { null },
    private val mapEvent: (OO) -> O? = { null }
) : Connectable<I, O> {

    override fun connect(output: Consumer<O>): Connection<I> {
        val delegate = effectHandler.connect { value ->
            mapEvent(value)?.let(output::accept)
        }
        return object : Connection<I> {
            override fun accept(value: I) {
                mapEffect(value)?.let(delegate::accept)
            }

            override fun dispose() {
                delegate.dispose()
            }
        }
    }
}

public inline fun <I, reified O, reified II, OO> Connectable<II, OO>.mapped(
    noinline mapEffect: (I) -> II? = { it as? II },
    noinline mapEvent: (OO) -> O? = { it as? O }
): Connectable<I, O> =
    MappedEffectHandler(
        this,
        mapEffect,
        mapEvent
    )
