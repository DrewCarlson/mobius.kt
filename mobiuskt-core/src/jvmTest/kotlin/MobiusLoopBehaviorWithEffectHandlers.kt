package kt.mobius

import com.google.common.util.concurrent.SettableFuture
import kt.mobius.Effects.effects
import kt.mobius.Next.Companion.next
import kt.mobius.functions.Consumer
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.test.TestWorkRunner
import kt.mobius.testdomain.*
import org.awaitility.Awaitility.await
import org.junit.Test
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors


class MobiusLoopBehaviorWithEffectHandlers : MobiusLoopTest() {
    @Test
    @Throws(Exception::class)
    fun shouldSurviveEffectPerformerThrowing() {
        mobiusLoop.dispatchEvent(EventWithCrashingEffect())
        mobiusLoop.dispatchEvent(TestEvent("should happen"))
        observer.assertStates("init", "will crash", "will crash->should happen")
    }

    @Test
    @Throws(Exception::class)
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
    @Throws(Exception::class)
    fun shouldSupportEffectsThatGenerateEvents() {
        setupWithEffects(
            Connectable { eventConsumer: Consumer<TestEvent> ->
                object : SimpleConnection<TestEffect> {
                    override fun accept(effect: TestEffect) {
                        eventConsumer.accept(TestEvent(effect.toString()))
                    }
                }
            },
            immediateRunner
        )
        mobiusLoop.dispatchEvent(EventWithSafeEffect("hi"))
        observer.assertStates("init", "init->hi", "init->hi->effecthi")
    }

    @Test
    @Throws(Exception::class)
    fun shouldOrderStateChangesCorrectlyWhenEffectsAreSlow() {
        val future = SettableFuture.create<TestEvent>()
        setupWithEffects(
            Connectable { eventConsumer: Consumer<TestEvent> ->
                object : SimpleConnection<TestEffect> {
                    override fun accept(effect: TestEffect) {
                        try {
                            eventConsumer.accept(future.get())
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        } catch (e: ExecutionException) {
                            throw RuntimeException(e)
                        }
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
            { eventConsumer: Consumer<TestEvent> ->
                object : SimpleConnection<TestEffect> {
                    override fun accept(effect: TestEffect) {
                        if (effect is SafeEffect) {
                            if (effect.id == "1") {
                                try {
                                    // Rough approximation of waiting infinite amount of time.
                                    Thread.sleep(2000)
                                } catch (e: InterruptedException) {
                                    // ignored.
                                }
                                return
                            }
                        }
                        eventConsumer.accept(TestEvent(effect.toString()))
                    }
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
            { eventConsumer: Consumer<TestEvent> ->
                object : SimpleConnection<TestEffect> {
                    override fun accept(effect: TestEffect) {
                        eventConsumer.accept(TestEvent(effect.toString()))
                    }
                }
            },
            testWorkRunner
        )
        //observer.waitForChange(100)
        testWorkRunner.runAll()
        observer.assertStates("init", "init->effectfrominit")
    }
}