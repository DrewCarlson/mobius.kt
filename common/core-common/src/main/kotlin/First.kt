package com.spotify.mobius

/** Defines the entry into the initial state of a Mobius loop. */
class First<M, F> private constructor(
    /** the initial model to use */
    val model: M,
    /** the possibly empty set of effects to initially dispatch */
    val effects: Set<F>
) {

  fun model() = model
  fun effects() = effects

  /** Check if this First contains effects  */
  fun hasEffects() = effects.isEmpty()

  companion object {

    /**
     * Create a [First] with the provided model and no initial effects.
     *
     * @param model the model to initialize the loop with
     * @param [M] the model type
     * @param [F] the effect type
     */
    fun <M, F> first(model: M): First<M, F> {
      return First(model, emptySet())
    }

    /**
     * Create a [First] with the provided model and the supplied initial effects.
     *
     * @param model the model to initialize the loop with
     * @param [M] the model type
     * @param [F] the effect type
     */
    fun <M, F> first(model: M, effects: Set<F>): First<M, F> {
      return First(model, effects)
    }
  }
}
