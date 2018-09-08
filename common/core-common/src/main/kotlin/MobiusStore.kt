package com.spotify.mobius

import kotlin.jvm.Volatile

/** Responsible for holding and updating the current model. */
class MobiusStore<M, E, F> private constructor(
    val init: Init<M, F>,
    val update: Update<M, E, F>,
    startModel: M
) {
  private object LOCK

  @Volatile
  private var currentModel: M = startModel

  fun init(): First<M, F> = synchronized(LOCK) {
    val first = init.init(currentModel!!)
    currentModel = first.model
    return first
  }

  fun update(event: E): Next<M, F> = synchronized(LOCK) {
    val next = update.update(currentModel, event)
    currentModel = next.modelOrElse(currentModel)
    return next
  }

  companion object {
    fun <M, E, F> create(init: Init<M, F>, update: Update<M, E, F>, startModel: M): MobiusStore<M, E, F> {
      return MobiusStore(init, update, startModel)
    }
  }
}
