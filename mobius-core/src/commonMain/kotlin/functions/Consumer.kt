package kt.mobius.functions


/** Interface for consuming values. */
interface Consumer<V> {
  companion object {
    operator fun <V> invoke(accept: (@ParameterName("value") V) -> Unit): Consumer<V> {
      return object : Consumer<V> {
        override fun accept(value: V) = accept(value)
      }
    }
  }

  @mpp.JsName("accept")
  fun accept(value: V)
}
