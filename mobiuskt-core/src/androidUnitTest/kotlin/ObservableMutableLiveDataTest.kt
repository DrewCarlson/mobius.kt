package kt.mobius.android

import androidx.arch.core.executor.testing.*
import androidx.lifecycle.*
import kt.mobius.disposables.*
import org.junit.*
import kotlin.test.*
import kotlin.test.Test

public class ObservableMutableLiveDataTest {
    @Rule
    @JvmField
    public var rule: InstantTaskExecutorRule = InstantTaskExecutorRule()
    private val lifecycleOwner1 = FakeLifecycleOwner()
    private val underTest = ObservableMutableLiveData<TestModel>()

    @Test
    public fun testDataSendsInactiveStateOnSubscribeAndThenActiveStateWhenObserverGoesActive() {
        val receivedEvents: MutableList<Boolean> = ArrayList(1)
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        underTest.observe(lifecycleOwner1) { }
        underTest.subscribe { e -> receivedEvents.add(e) }
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, receivedEvents.size)
        assertTrue(receivedEvents.first())
    }

    @Test
    public fun testDataSendsActiveStateOnSubscribeAndThenInactiveStateWhenObserverGoesInactive() {
        val receivedEvents: MutableList<Boolean> = ArrayList(1)
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        underTest.observe(lifecycleOwner1) { }
        underTest.subscribe { e -> receivedEvents.add(e) }
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        assertEquals(1, receivedEvents.size)
        assertFalse(receivedEvents.first())
    }

    @Test
    public fun testThatDisposeRemovesObserverFromLiveData() {
        val receivedEvents: MutableList<Boolean> = ArrayList(1)
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        underTest.observe(lifecycleOwner1) { }
        underTest.subscribe { e -> receivedEvents.add(e) }.dispose()
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(0, receivedEvents.size)
    }

    private var d: Disposable? = null

    @Test
    public fun testThatDisposingFromObserverCallbackDoesNotBreak() {
        val receivedEvents: MutableList<Boolean> = ArrayList(1)
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_START)
        underTest.observe(lifecycleOwner1) { }
        d = underTest.subscribe { value: Boolean ->
            receivedEvents.add(value)
            d!!.dispose()
        }
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_START)

        // first emitted event recorded is the ON_STOP, which then immediately disposes
        assertEquals(1, receivedEvents.size)
        assertFalse(receivedEvents.first())
    }
}
