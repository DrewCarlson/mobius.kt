package kt.mobius.android

import androidx.lifecycle.*

internal class FakeLifecycleOwner internal constructor() : LifecycleOwner {
    private val owner: LifecycleOwner = LifecycleOwner { LifecycleRegistry(this) }
    private val lifecycle: LifecycleRegistry = owner.lifecycle as LifecycleRegistry

    val currentState: Lifecycle.State
        get() = lifecycle.currentState

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycle.handleLifecycleEvent(event)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }
}
