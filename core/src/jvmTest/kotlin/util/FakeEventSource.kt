package com.spotify.mobius

import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import java.util.ArrayList

internal class FakeEventSource<E> : EventSource<E> {

  private val myConsumers = ArrayList<Consumer<E>>()

  fun emit(toEmit: E) {
    for (myConsumer in myConsumers) {
      myConsumer.accept(toEmit)
    }
  }

  override fun subscribe(eventConsumer: Consumer<E>): Disposable {
    myConsumers.add(eventConsumer)

    return object : Disposable {
      override fun dispose() {
        // no-op for now; add a 'disposed' flag or something if needed later
      }
    }
  }
}
