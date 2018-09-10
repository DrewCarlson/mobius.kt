package com.spotify.mobius

import com.spotify.mobius.disposables.CompositeDisposable
import com.spotify.mobius.functions.Consumer
import synchronized2

/**
 * A [Connectable] that ensures that an inner [Connection] doesn't emit or receive any
 * values after being disposed.
 *
 *
 * This only acts as a safeguard, you still need to make sure that the Connectable disposes of
 * resources correctly.
 */
internal class SafeConnectable<F, E>(
    private val actual: Connectable<F, E>
) : Connectable<F, E> {

  override fun connect(output: Consumer<E>): Connection<F> {
    val safeEventConsumer = SafeConsumer(output)
    val effectConsumer = SafeEffectConsumer(actual.connect(safeEventConsumer))
    val disposable = CompositeDisposable(safeEventConsumer, effectConsumer)
    return object : Connection<F> {
      override fun accept(effect: F): Unit = synchronized2(this) {
        effectConsumer.accept(effect)
      }

      override fun dispose(): Unit = synchronized2(this) {
        disposable.dispose()
      }
    }
  }

  class SafeEffectConsumer<F>(private val actual: Connection<F>) : Connection<F> {
    private object LOCK
    private var disposed: Boolean = false

    override fun accept(effect: F): Unit = synchronized2(LOCK) {
      if (disposed) {
        return
      }
      actual.accept(effect)
    }

    override fun dispose(): Unit = synchronized2(LOCK) {
      disposed = true
      actual.dispose()
    }
  }

  class SafeConsumer<E>(private val actual: Consumer<E>) : Connection<E> {
    private object LOCK
    private var disposed: Boolean = false

    override fun accept(value: E): Unit = synchronized2(LOCK) {
      if (disposed) {
        return
      }
      actual.accept(value)
    }

    override fun dispose(): Unit = synchronized2(LOCK) {
      disposed = true
    }
  }
}
