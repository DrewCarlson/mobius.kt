package kt.mobius

import com.google.common.util.concurrent.SettableFuture
import kt.mobius.Effects.effects
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kt.mobius.test.TestWorkRunner
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Before
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*


class MobiusLoopTest {

  private lateinit var mobiusLoop: MobiusLoop<String, TestEvent, TestEffect>
  private lateinit var mobiusStore: MobiusStore<String, TestEvent, TestEffect>
  private lateinit var effectHandler: Connectable<TestEffect, TestEvent>

  private val immediateRunner = ImmediateWorkRunner()
  private val backgroundRunner =
      ExecutorServiceWorkRunner(Executors.newSingleThreadExecutor())

  private var eventSource =
      EventSource<TestEvent> {
        Disposable {
        }
      }

  private lateinit var observer: RecordingModelObserver<String>
  private lateinit var update: Update<String, TestEvent, TestEffect>

  @Before
  fun setUp() {
    val init = Init<String, TestEffect> { model ->
      First.first(model)
    }

    update = Update { model, mobiusEvent ->
      when (mobiusEvent) {
        is TestEvent.EventWithCrashingEffect ->
          Next.next("will crash", effects(TestEffect.Crash))
        is TestEvent.EventWithSafeEffect -> Next.next(
            model + "->" + mobiusEvent.toString(), setOf<TestEffect>(TestEffect.SafeEffect(mobiusEvent.toString())))
        else -> Next.next(model + "->" + mobiusEvent.toString())
      }
    }

    mobiusStore = MobiusStore.create(init, update, "init")

    effectHandler = Connectable {
      SimpleConnection { effect ->
          if (effect is TestEffect.Crash) {
            throw RuntimeException("Crashing!")
          }
        }
      }

    setupWithEffects(effectHandler, immediateRunner)
  }

  @Test
  fun shouldTransitionToNextStateBasedOnInput() {
    mobiusLoop.dispatchEvent(TestEvent.Simple ("first"))
    mobiusLoop.dispatchEvent(TestEvent.Simple ("second"))

    observer.assertStates("init", "init->first", "init->first->second")
  }

  @Test
  fun shouldSurviveEffectPerformerThrowing() {
    mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
    mobiusLoop.dispatchEvent(TestEvent.Simple("should happen"))

    observer.assertStates("init", "will crash", "will crash->should happen")
  }

  @Test
  fun shouldSurviveEffectPerformerThrowingMultipleTimes() {
    mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
    mobiusLoop.dispatchEvent(TestEvent.Simple("should happen"))
    mobiusLoop.dispatchEvent(TestEvent.EventWithCrashingEffect)
    mobiusLoop.dispatchEvent(TestEvent.Simple("should happen, too"))

    observer.assertStates(
        "init",
        "will crash",
        "will crash->should happen",
        "will crash",
        "will crash->should happen, too")
  }

  @Test
  fun shouldSupportEffectsThatGenerateEvents() {
    setupWithEffects(Connectable {eventConsumer ->
      SimpleConnection { effect ->
        eventConsumer.accept(TestEvent.Simple(effect.toString()))
      }
    }, immediateRunner)

    mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("hi"))

    observer.assertStates("init", "init->hi", "init->hi->effecthi")
  }

  @Test
  fun shouldOrderStateChangesCorrectlyWhenEffectsAreSlow() {
    val future = SettableFuture.create<TestEvent>()

    setupWithEffects(Connectable { eventConsumer ->
      SimpleConnection<TestEffect> { _ ->

        try {
          eventConsumer.accept(future.get())

        } catch (e: InterruptedException) {
          e.printStackTrace()
        } catch (e: ExecutionException) {
          e.printStackTrace()
        }
      }
    }, backgroundRunner)

    mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("1"))
    mobiusLoop.dispatchEvent(TestEvent.Simple("2"))

    await().atMost(Duration.ONE_SECOND).until { observer.valueCount() >= 3 }

    future.set(TestEvent.Simple("3"))

    await().atMost(Duration.ONE_SECOND).until { observer.valueCount() >= 4 }
    observer.assertStates("init", "init->1", "init->1->2", "init->1->2->3")
  }

  @Test
  fun shouldSupportHandlingEffectsWhenOneEffectNeverCompletes() {
    setupWithEffects(Connectable { eventConsumer ->
      SimpleConnection { effect ->

        if (effect is TestEffect.SafeEffect) {
          if (effect.id == "1") {
            try {
              // Rough approximation of waiting infinite amount of time.
              Thread.sleep(2000)
            } catch (e: InterruptedException) {
              // ignored.
            }

            return@SimpleConnection
          }
        }
        eventConsumer.accept(TestEvent.Simple(effect.toString()))
      }
    }, ExecutorServiceWorkRunner(Executors.newFixedThreadPool(2)))

    // the effectHandler associated with "1" should never happen
    mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("1"))
    mobiusLoop.dispatchEvent(TestEvent.Simple("2"))
    mobiusLoop.dispatchEvent(TestEvent.EventWithSafeEffect("3"))

    await().atMost(Duration.FIVE_SECONDS).until { observer.valueCount() >= 5 }

    observer.assertStates(
        "init", "init->1", "init->1->2", "init->1->2->3", "init->1->2->3->effect3")
  }

  @Test
  fun shouldPerformEffectFromInit() {
    val init = Init<String, TestEffect> { model ->
      First.first(model, setOf<TestEffect>(TestEffect.SafeEffect("frominit")))
    }

    val update =
        Update<String, TestEvent, TestEffect> { model, event ->
          Next.next(model + "->" + event.toString())
        }


    mobiusStore = MobiusStore.create(init, update, "init")
    val testWorkRunner = TestWorkRunner()

    setupWithEffects(Connectable { eventConsumer ->
      SimpleConnection { effect ->
        eventConsumer.accept(TestEvent.Simple(effect.toString()))
      }
    }, testWorkRunner)

    //TODO: observer.waitForChange(100)
    testWorkRunner.runAll()

    observer.assertStates("init", "init->effectfrominit")
  }

  @Test
  fun dispatchingEventsAfterDisposalThrowsException() {
    mobiusLoop.dispose()

    assertFailsWith(IllegalStateException::class) {
      mobiusLoop.dispatchEvent(TestEvent.Simple("2"))
    }
  }

  @Test
  fun shouldSupportUnregisteringObserver() {
    observer = RecordingModelObserver()

    mobiusLoop =
        MobiusLoop.create(
            mobiusStore, effectHandler, eventSource, immediateRunner, immediateRunner);

    val unregister = mobiusLoop.observe(observer)

    mobiusLoop.dispatchEvent(TestEvent.Simple("active observer"))
    unregister.dispose()
    mobiusLoop.dispatchEvent(TestEvent.Simple("shouldn't be seen"))

    observer.assertStates("init", "init->active observer")
  }

  @Test
  fun shouldThrowForEventSourceEventsAfterDispose() {
    val eventSource = FakeEventSource<TestEvent>()

    mobiusLoop =
        MobiusLoop.create(
            mobiusStore, effectHandler, eventSource, immediateRunner, immediateRunner)

    observer = RecordingModelObserver() // to clear out the init from the previous setup
    mobiusLoop.observe(observer)

    eventSource.emit(TestEvent.EventWithSafeEffect("one"))
    mobiusLoop.dispose()


    assertFailsWith(IllegalStateException::class) {
      eventSource.emit(TestEvent.EventWithSafeEffect("two"))
    }

    observer.assertStates("init", "init->one")
  }

  @Test
  fun shouldThrowForEffectHandlerEventsAfterDispose() {
    val effectHandler = FakeEffectHandler()

    setupWithEffects(effectHandler, immediateRunner);

    effectHandler.emitEvent(TestEvent.EventWithSafeEffect("good one"))

    mobiusLoop.dispose()

    assertFailsWith(IllegalStateException::class) {
      effectHandler.emitEvent(TestEvent.EventWithSafeEffect("bad one"))
    }

    observer.assertStates("init", "init->good one")
  }

  @Test
  fun shouldProcessInitBeforeEventsFromEffectHandler() {
    mobiusStore = MobiusStore.create(Init { First.first("I$it") }, update, "init")

    // when an effect handler that emits events before returning the connection
    setupWithEffects(
        Connectable { output ->
          output.accept(TestEvent.Simple("1"))

          SimpleConnection {
          }
        },
        immediateRunner)

    // in this scenario, the init and the first event get processed before the observer
    // is connected, meaning the 'Iinit' state is never seen
    observer.assertStates("Iinit->1")
  }

  @Test
  fun shouldProcessInitBeforeEventsFromEventSource() {
    mobiusStore = MobiusStore.create(Init { First.first("First$it") }, update, "init")

    eventSource =
        EventSource { eventConsumer ->
          eventConsumer.accept(TestEvent.Simple("1"))
          Disposable {
            // do nothing
          }
        }

    setupWithEffects(FakeEffectHandler(), immediateRunner)

    // in this scenario, the init and the first event get processed before the observer
    // is connected, meaning the 'Firstinit' state is never seen
    observer.assertStates("Firstinit->1")
  }

  private fun setupWithEffects(
      effectHandler: Connectable<TestEffect, TestEvent>, effectRunner: WorkRunner) {
    observer = RecordingModelObserver()

    mobiusLoop =
        MobiusLoop.create(mobiusStore, effectHandler, eventSource, immediateRunner, effectRunner)

    mobiusLoop.observe(observer)
  }

  private sealed class TestEvent(
      private val name: String
  ) {

    class Simple(name: String) : TestEvent(name)

    object EventWithCrashingEffect : TestEvent("crash!")

    class EventWithSafeEffect(id: String) : TestEvent(id)

    override fun toString(): String {
      return name
    }
  }

  private sealed class TestEffect {
    object Crash : TestEffect()

    data class SafeEffect(
        val id: String
    ) : TestEffect() {

      override fun toString(): String {
        return "effect$id"
      }
    }
  }

  private class FakeEffectHandler : Connectable<TestEffect, TestEvent> {

    @Volatile
    private var eventConsumer: Consumer<TestEvent>? = null

    fun emitEvent(event: TestEvent) {
      // throws NPE if not connected; that's OK
      eventConsumer!!.accept(event)
    }

    override fun connect(output: Consumer<TestEvent>): Connection<TestEffect> {
      if (eventConsumer != null) {
        throw ConnectionLimitExceededException()
      }

      eventConsumer = output

      return object : Connection<TestEffect> {
        override fun accept(value: TestEffect) {
          // do nothing
        }

        override fun dispose() {
          // do nothing
        }
      };
    }
  }
}
