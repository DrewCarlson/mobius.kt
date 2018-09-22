package com.spotify.mobius

import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer


/**
 * An [EventSource] that merges multiple sources into one
 *
 * @param E The type of Events the sources will emit
 */
class MergedEventSource<E> private constructor(
    private val eventSources: List<EventSource<E>>
) : EventSource<E> {

  override fun subscribe(eventConsumer: Consumer<E>): Disposable {
    val disposables = ArrayList<Disposable>(eventSources.size)
    for (eventSource in eventSources) {
      disposables.add(eventSource.subscribe(eventConsumer))
    }

    return object : Disposable {
      override fun dispose() {
        for (disposable in disposables) {
          disposable.dispose()
        }
      }
    }
  }

  companion object {
    @mpp.JvmStatic
    @mpp.JsName("from")
    fun <E> from(vararg eventSources: EventSource<E>): EventSource<E> {
      val allSources = ArrayList<EventSource<E>>()
      allSources.addAll(eventSources)
      return MergedEventSource(allSources)
    }
  }
}
