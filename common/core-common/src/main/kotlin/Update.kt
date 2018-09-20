package com.spotify.mobius

/**
 * An interface representing the Update function used by Mobius for performing model transitions and
 * requesting side-effects.
 *
 *
 * Implementations of this interface must be pure - they should have no model and no
 * side-effects. This means that given the same arguments, the function must always return the same
 * Next. Each time a new event occurs, the update method will be called and a Next is expected.
 */
interface Update<M, E, F> {
  companion object {
    operator fun <M, E, F> invoke(
        update: (@ParameterName("model") M, @ParameterName("event") E) -> Next<M, F>
    ): Update<M, E, F> {
      return object : Update<M, E, F> {
        override fun update(model: M, event: E): Next<M, F> {
          return update(model, event)
        }
      }
    }
  }

  @mpp.JsName("update")
  fun update(model: M, event: E): Next<M, F>
}
