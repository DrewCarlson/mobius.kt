package kt.mobius

interface SimpleConnection<F> : Connection<F> {

  companion object {
    operator fun <F> invoke(consumer: (F) -> Unit): SimpleConnection<F> {
      return object : SimpleConnection<F> {
        override fun accept(value: F) {
          consumer(value)
        }
      }
    }
  }

  override fun dispose() {
    // ignored.
  }
}
