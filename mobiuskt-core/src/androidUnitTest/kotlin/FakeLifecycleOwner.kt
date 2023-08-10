package kt.mobius.android

import androidx.lifecycle.*

internal class FakeLifecycleOwner internal constructor() : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    val currentState: Lifecycle.State
        get() = lifecycle.currentState

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}
