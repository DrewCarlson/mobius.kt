package kt.mobius.extras

import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.functions.Consumer

/**
 * Creates a [Connectable] that delegates connection creation to [effectHandlers]
 * and the corresponding [Connection]s.
 */
class CompositeEffectHandler<I, O> private constructor(
  private val effectHandlers: Array<out Connectable<I, O>>
) : Connectable<I, O> {

  companion object {
    @mpp.JvmStatic
    @mpp.JsName("from")
    fun <I, O> from(vararg effectHandlers: Connectable<I, O>) =
      CompositeEffectHandler(effectHandlers)
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
