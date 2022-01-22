package kt.mobius.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.*
import kt.mobius.runners.*
import kt.mobius.test.*
import org.junit.Rule
import org.junit.rules.TestRule
import java.util.*
import kotlin.test.*

public class MutableLiveQueueTest {
    @Rule
    @JvmField
    public var rule: TestRule = InstantTaskExecutorRule()
    private lateinit var mutableLiveQueue: MutableLiveQueue<String>
    private lateinit var fakeLifecycleOwner1: FakeLifecycleOwner
    private lateinit var fakeLifecycleOwner2: FakeLifecycleOwner
    private lateinit var liveObserver: RecordingObserver<String>
    private lateinit var pausedObserver: RecordingObserver<Iterable<String>>

    @BeforeTest
    public fun setup() {
        mutableLiveQueue = MutableLiveQueue(WorkRunners.immediate(), QUEUE_CAPACITY)
        fakeLifecycleOwner1 = FakeLifecycleOwner()
        fakeLifecycleOwner2 = FakeLifecycleOwner()
        liveObserver = RecordingObserver()
        pausedObserver = RecordingObserver()
    }

    @Test
    public fun shouldIgnoreDestroyedLifecycleOwner() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver)
        assertFalse(mutableLiveQueue.hasObserver())
    }

    @Test
    public fun shouldSendDataToResumedObserver() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver)
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        assertTrue(mutableLiveQueue.hasActiveObserver())
        liveObserver.assertValues("one", "two")
    }

    @Test
    public fun shouldNotSendToLiveObserverIfClearedBeforeRunnerExecutes() {
        val testWorkRunner = TestWorkRunner()
        mutableLiveQueue = MutableLiveQueue(testWorkRunner, QUEUE_CAPACITY)
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver)
        mutableLiveQueue.post("one")
        mutableLiveQueue.clearObserver()
        testWorkRunner.runAll()
        assertFalse(mutableLiveQueue.hasActiveObserver())
        assertEquals(0, liveObserver.valueCount())
        assertEquals(0, pausedObserver.valueCount())
    }

    @Test
    public fun shouldQueueEventsWithNoObserver() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver, pausedObserver)
        assertEquals(0, liveObserver.valueCount())
        assertEquals(1, pausedObserver.valueCount())
    }

    @Test
    public fun shouldSendQueuedEventsWithValidPausedObserver() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver, pausedObserver)
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(0, liveObserver.valueCount())
        pausedObserver.assertValues(queueOf("one", "two"))
    }

    @Test
    public fun shouldSendLiveAndQueuedEventsWhenRunningAndThenPausedObserver() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver, pausedObserver)
        mutableLiveQueue.post("one")
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.post("two")
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        liveObserver.assertValues("one")
        pausedObserver.assertValues(queueOf("two"))
    }

    @Test
    public fun shouldNotSendQueuedEffectsIfPausedObserverClearedBeforeRunnerCanExecute() {
        val testWorkRunner = TestWorkRunner()
        mutableLiveQueue = MutableLiveQueue(testWorkRunner, QUEUE_CAPACITY)
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(fakeLifecycleOwner2, liveObserver, pausedObserver)
        mutableLiveQueue.post("one")
        fakeLifecycleOwner2.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.clearObserver()
        testWorkRunner.runAll()
        assertFalse(mutableLiveQueue.hasActiveObserver())
        assertEquals(0, liveObserver.valueCount())
        assertEquals(0, pausedObserver.valueCount())
    }

    @Test
    public fun shouldSendQueuedEffectsIfObserverSwappedToResumedOneClearing() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(
            fakeLifecycleOwner1,
            { s: String? -> }
        ) { s: Iterable<String?>? -> }
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        fakeLifecycleOwner2.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        mutableLiveQueue.setObserver(fakeLifecycleOwner2, liveObserver, pausedObserver)
        assertEquals(0, liveObserver.valueCount())
        pausedObserver.assertValues(queueOf("one", "two"))
    }

    @Test
    public fun shouldSendQueuedEffectsIfObserverSwappedWithoutClearing() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(
            fakeLifecycleOwner1,
            { s: String? -> }
        ) { s: Iterable<String?>? -> }
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        mutableLiveQueue.setObserver(fakeLifecycleOwner2, liveObserver, pausedObserver)
        fakeLifecycleOwner2.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(0, liveObserver.valueCount())
        pausedObserver.assertValues(queueOf("one", "two"))
    }

    @Test
    public fun shouldClearQueueIfObserverCleared() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(
            fakeLifecycleOwner1,
            { s: String? -> }
        ) { s: Iterable<String?>? -> }
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        mutableLiveQueue.clearObserver()
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver, pausedObserver)
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(0, liveObserver.valueCount())
        assertEquals(0, pausedObserver.valueCount())
    }

    @Test
    public fun shouldClearQueueIfLifecycleDestroyed() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver, pausedObserver)
        mutableLiveQueue.post("one")
        mutableLiveQueue.post("two")
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertEquals(0, liveObserver.valueCount())
        assertEquals(0, pausedObserver.valueCount())
    }

    @Test
    public fun shouldThrowIllegalStateExceptionIfQueueFull() {
        fakeLifecycleOwner1.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mutableLiveQueue.setObserver(fakeLifecycleOwner1, liveObserver)
        mutableLiveQueue.post("1")
        mutableLiveQueue.post("2")
        mutableLiveQueue.post("3")
        mutableLiveQueue.post("4")
        val error = assertFails { mutableLiveQueue.post("this one breaks")  }
        assertIs<IllegalStateException>(error)
        assertTrue(error.message.orEmpty().contains("this one breaks"))
        assertTrue(error.message.orEmpty().contains(QUEUE_CAPACITY.toString()))
    }

    private fun queueOf(vararg args: String): Queue<String> {
        return LinkedList(args.toList())
    }

    public companion object {
        public const val QUEUE_CAPACITY: Int = 4
    }
}
