package kt.mobius.android

import androidx.arch.core.executor.testing.*
import androidx.lifecycle.*
import kt.mobius.*
import kt.mobius.First.Companion.first
import kt.mobius.Mobius.loop
import kt.mobius.Next.Companion.noChange
import kt.mobius.runners.*
import org.junit.*
import kotlin.test.*
import kotlin.test.Test

public class MobiusLoopViewModelTest {
    @Rule
    public var rule: InstantTaskExecutorRule = InstantTaskExecutorRule()
    private var recordedEvents: MutableList<TestEvent> = ArrayList()
    private val updateFunction = Update { _: TestModel, event: TestEvent ->
        recordedEvents.add(event)
        noChange<TestModel, TestEffect>()
    }
    private lateinit var underTest: MobiusLoopViewModel<TestModel, TestEvent, TestEffect, TestViewEffect>
    private var testViewEffectHandler: TestViewEffectHandler<TestEvent, TestEffect, TestViewEffect>? = null
    private var fakeLifecycle: FakeLifecycleOwner? = null
    private var recordingModelObserver = RecordingObserver<TestModel>()
    private var recordingForegroundViewEffectObserver = RecordingObserver<TestViewEffect>()
    private var recordingBackgroundEffectObserver = RecordingObserver<Iterable<TestViewEffect>>()
    private lateinit var initialModel: TestModel

    @BeforeTest
    public fun setUp() {
        fakeLifecycle = FakeLifecycleOwner()
        recordedEvents = ArrayList()
        testViewEffectHandler = null
        recordingModelObserver = RecordingObserver()
        recordingForegroundViewEffectObserver = RecordingObserver()
        recordingBackgroundEffectObserver = RecordingObserver()
        initialModel = TestModel("initial model")
        underTest = MobiusLoopViewModel.create(
            { consumer, _ ->
                testViewEffectHandler = TestViewEffectHandler(consumer)
                loop(updateFunction, testViewEffectHandler!!)
                    .eventRunner { ImmediateWorkRunner() }
                    .effectRunner { ImmediateWorkRunner() }
            },
            initialModel,
            { model: TestModel -> first(model) },
            ImmediateWorkRunner(),
            100
        )
        underTest.models.observe(fakeLifecycle!!, recordingModelObserver)
        underTest
            .viewEffects
            .setObserver(
                fakeLifecycle!!,
                recordingForegroundViewEffectObserver,
                recordingBackgroundEffectObserver
            )
    }

    @Test
    public fun testViewModelgetModelAtStartIsInitialModel() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals("initial model", underTest.model.name)
        recordingModelObserver.assertValues(initialModel)
    }

    @Test
    public fun testViewModelSendsEffectsIntoLoop() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        underTest.dispatchEvent(TestEvent("testable"))
        assertEquals(1, recordedEvents.size)
        assertEquals("testable", recordedEvents[0].name)
    }

    @Test
    public fun testViewModelDoesNotSendViewEffectsIfLifecycleIsPaused() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        testViewEffectHandler!!.viewEffectConsumer.accept(TestViewEffect("view effect 1"))
        assertEquals(0, recordingForegroundViewEffectObserver.valueCount())
        assertEquals(0, recordingBackgroundEffectObserver.valueCount())
    }

    @Test
    public fun testViewModelSendsViewEffectsToBackgroundObserverWhenLifecycleWasPausedThenIsResumed() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        testViewEffectHandler!!.viewEffectConsumer.accept(TestViewEffect("view effect 1"))
        assertEquals(0, recordingBackgroundEffectObserver.valueCount())
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, recordingBackgroundEffectObserver.valueCount())
    }

    @Test
    public fun testViewModelSendsViewEffectsToForegroundObserverWhenLifecycleIsResumed() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        testViewEffectHandler!!.viewEffectConsumer.accept(TestViewEffect("view effect 1"))
        assertEquals(1, recordingForegroundViewEffectObserver.valueCount())
        assertEquals(0, recordingBackgroundEffectObserver.valueCount())
    }

    @Test
    public fun testViewModelDoesNotTryToForwardEventsIntoLoopAfterCleared() {
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        underTest.onCleared()
        underTest.dispatchEvent(TestEvent("don't record me"))
        assertEquals(0, recordedEvents.size)
    }

    @Test
    public fun testViewEffectsPostedImmediatelyAreSentCorrectly() {
        underTest = MobiusLoopViewModel.create(
            { consumer, _ ->
                val viewEffectSendingEffectHandler = ViewEffectSendingEffectHandler(consumer)
                testViewEffectHandler = TestViewEffectHandler(consumer)
                loop(updateFunction, viewEffectSendingEffectHandler)
                    .eventRunner { ImmediateWorkRunner() }
                    .effectRunner { ImmediateWorkRunner() }
            },
            initialModel,
            { model: TestModel ->
                first(model, effects(TestEffect("oops")))
            },
            ImmediateWorkRunner(),
            100
        )
        underTest
            .viewEffects
            .setObserver(
                fakeLifecycle!!,
                recordingForegroundViewEffectObserver,
                recordingBackgroundEffectObserver
            )
        fakeLifecycle!!.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(0, recordingForegroundViewEffectObserver.valueCount())
        assertEquals(1, recordingBackgroundEffectObserver.valueCount())
    }

    private fun effects(vararg effects: TestEffect): Set<TestEffect> {
        return effects.toSet()
    }
}
