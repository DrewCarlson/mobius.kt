package com.spotify.mobius

/**
 * An interface representing the Init function used by Mobius for starting or resuming execution of
 * a program from a given model.
 *
 *
 * Implementations of this interface must be pure - they should have no model and no
 * side-effects. This means that given the same arguments, the function must always return the same
 * [First].
 */
interface Init<M, F> {
  companion object {
    inline operator fun <M, F> invoke(crossinline init: (M) -> First<M, F>): Init<M, F> {
      return object : Init<M, F> {
        override fun init(model: M): First<M, F> {
          return init(model)
        }
      }
    }
  }

  fun init(model: M): First<M, F>
}
