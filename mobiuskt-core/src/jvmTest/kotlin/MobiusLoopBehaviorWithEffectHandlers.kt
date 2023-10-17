package kt.mobius

import com.google.common.util.concurrent.SettableFuture
import kt.mobius.Effects.effects
import kt.mobius.Next.Companion.next
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.test.SimpleConnection
import kt.mobius.test.TestWorkRunner
import kt.mobius.testdomain.*
import org.awaitility.Awaitility.await
import org.junit.Test
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors


class MobiusLoopBehaviorWithEffectHandlers : MobiusLoopTest() {
    @Test
    fun shouldSurviveEffectPerformerThrowing() {
        mobiusLoop.dispatchEvent(EventWithCrashingEffect())
        mobiusLoop.dispatchEvent(TestEvent("should happen"))
        observer.assertStates("init", "will crash", "will crash->should happen")
    }

    @Test
    fun shouldSurviveEffectPerformerThrowingMultipleTimes() {
        mobiusLoop.dispatchEvent(EventWithCrashingEffect())
        mobiusLoop.dispatchEvent(TestEvent("should happen"))
        mobiusLoop.dispatchEvent(EventWithCrashingEffect())
        mobiusLoop.dispatchEvent(TestEvent("should happen, too"))
        observer.assertStates(
            "init",
            "will crash",
            "will crash->should happen",
            "will crash",
            "will crash->should happen, too"
        )
    }

    @Test
    fun shouldSupportEffectsThatGenerateEvents() {
        setupWithEffects(
            { eventConsumer ->
                SimpleConnection { value ->
                    eventConsumer.accept(TestEvent(value.toString()))
                }
            },
            immediateRunner
        )
        mobiusLoop.dispatchEvent(EventWithSafeEffect("hi"))
        observer.assertStates("init", "init->hi", "init->hi->effecthi")
    }

    @Test
    fun shouldOrderStateChangesCorrectlyWhenEffectsAreSlow() {
        val future = SettableFuture.create<TestEvent>()
        setupWithEffects(
            { eventConsumer ->
                SimpleConnection {
                    try {
                        eventConsumer.accept(future.get())
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    } catch (e: ExecutionException) {
                        throw RuntimeException(e)
                    }
                }
            },
            backgroundRunner
        )
        mobiusLoop.dispatchEvent(EventWithSafeEffect("1"))
        mobiusLoop.dispatchEvent(TestEvent("2"))
        await().atMost(Duration.ofSeconds(1)).until { observer.valueCount() >= 3 }
        future.set(TestEvent("3"))
        await().atMost(Duration.ofSeconds(1)).until { observer.valueCount() >= 4 }
        observer.assertStates("init", "init->1", "init->1->2", "init->1->2->3")
    }

    @Test
    fun shouldSupportHandlingEffectsWhenOneEffectNeverCompletes() {
        setupWithEffects(
            { eventConsumer ->
                SimpleConnection { value ->
                    if (value is SafeEffect) {
                        if (value.id == "1") {
                            try {
                                // Rough approximation of waiting infinite amount of time.
                                Thread.sleep(2000)
                            } catch (e: InterruptedException) {
                                // ignored.
                            }
                            return@SimpleConnection
                        }
                    }
                    eventConsumer.accept(TestEvent(value.toString()))
                }
            },
            ExecutorServiceWorkRunner(Executors.newFixedThreadPool(2))
        )

        // the effectHandler associated with "1" should never happen
        mobiusLoop.dispatchEvent(EventWithSafeEffect("1"))
        mobiusLoop.dispatchEvent(TestEvent("2"))
        mobiusLoop.dispatchEvent(EventWithSafeEffect("3"))
        await().atMost(Duration.ofSeconds(5)).until { observer.valueCount() >= 5 }
        observer.assertStates(
            "init", "init->1", "init->1->2", "init->1->2->3", "init->1->2->3->effect3"
        )
    }

    @Test
    fun shouldPerformEffectFromInit() {
        update = Update { model, event -> next("$model->$event") }
        startModel = "init"
        startEffects = effects(SafeEffect("frominit"))
        val testWorkRunner = TestWorkRunner()
        setupWithEffects(
            { eventConsumer ->
                SimpleConnection { value ->
                    eventConsumer.accept(TestEvent(value.toString()))
                }
            },
            testWorkRunner
        )
        //observer.waitForChange(100)
        testWorkRunner.runAll()
        observer.assertStates("init", "init->effectfrominit")
    }
}
