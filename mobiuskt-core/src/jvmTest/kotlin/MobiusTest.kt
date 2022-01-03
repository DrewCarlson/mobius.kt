package kt.mobius

import kt.mobius.First.Companion.first
import kt.mobius.Mobius.loop
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kt.mobius.runners.WorkRunners
import kt.mobius.test.SimpleConnection
import org.awaitility.Awaitility.await
import org.hamcrest.Matchers.contains
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.Nonnull
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue


class MobiusTest {

    private val UPDATE = Update<String, Int, Boolean> { model, event ->
        Next.next(
            model + event,
            setOf(event % 2 == 0)
        )
    }

    private val HANDLER = Connectable<Boolean, Int> { output ->
        object : SimpleConnection<Boolean> {
            override fun accept(value: Boolean) {
                if (value) {
                    output.accept(3)
                }
            }
        }
    }


    private val MY_MODEL = "start"
    private lateinit var loop: MobiusLoop<String, Int, Boolean>

    @Test
    fun shouldInstantiateWithMinimumParams() {
        loop = loop(UPDATE, HANDLER).startFrom(MY_MODEL)
        loop.dispatchEvent(8)
        await().atMost(Duration.ofSeconds(1)).until({ loop.mostRecentModel }, `is`("start83"))
    }

    @Suppress("DEPRECATION")
    @Test
    fun shouldPermitUsingCustomInit() {
        val init: Init<String, Boolean> = Init { model -> First.first(model, true) }
        loop = loop(UPDATE, HANDLER).init(init).startFrom(MY_MODEL)
        loop.dispatchEvent(3)
        await().atMost(Duration.ofSeconds(1)).until { "start33" == loop.mostRecentModel }
    }


    @Test
    fun shouldPermitUsingCustomEffectRunner() {
        val runner = TestableWorkRunner()
        loop = loop(UPDATE, HANDLER).effectRunner { runner }.startFrom(MY_MODEL)
        loop.dispatchEvent(3)
        await().atMost(Duration.ofSeconds(1)).until { runner.runCounter.get() == 1 }
    }

    @Test
    fun shouldPermitUsingCustomEventRunner() {
        val runner = TestableWorkRunner()
        loop = loop(UPDATE, HANDLER).eventRunner { runner }.startFrom(MY_MODEL)
        loop.dispatchEvent(3)
        await().atMost(Duration.ofSeconds(1)).until { runner.runCounter.get() == 1 }
    }


    @Test
    fun shouldPermitUsingEventSource() {
        val eventSource = TestEventSource()

        loop = loop(UPDATE, HANDLER)
            .eventRunner(WorkRunners::immediate)
            .eventSource(eventSource)
            .startFrom(MY_MODEL)

        eventSource.consumer.accept(7)

        await().atMost(Duration.ofSeconds(1)).until({ loop.mostRecentModel }, `is`("start7"))
    }


    @Test
    fun shouldPermitUsingConnectablesAsAnEventSource() {
        val eventSource = ConnectableTestEventSource()

        loop = loop(UPDATE, HANDLER)
                .eventRunner(WorkRunners::immediate)
                .eventSource(eventSource)
                .startFrom(MY_MODEL)

        eventSource.consumer.accept(7)

        await().atMost(Duration.ofSeconds(1)).until({loop.mostRecentModel}, `is`("start7"))
    }


    @Suppress("DEPRECATION")
    @Test
    fun shouldPermitUsingCustomLogger() {
        val logger = TestLogger()
        loop = loop(UPDATE, HANDLER)
            .init(Init { first(it) })
            .logger(logger)
            .eventRunner { ImmediateWorkRunner() }
            .effectRunner { ImmediateWorkRunner() }
            .startFrom(MY_MODEL)
        loop.dispatchEvent(7)
        assertThat(
            logger.history,
            contains(
                "before init: start",
                "after init: start, First(model=start, effects=[])",
                "before update: start, 7",
                "after update: start, 7, Next(model=start7, effects=[false])"
            )
        )
    }

    @Test
    fun shouldSupportCreatingFactory() {
        val factory: MobiusLoop.Factory<String, Int, Boolean> = loop(UPDATE, HANDLER)
        loop = factory.startFrom("resume")
        loop.dispatchEvent(97)
        await().atMost(Duration.ofSeconds(1)).until({ loop.mostRecentModel }, `is`("resume97"))
    }

    @Test
    fun shouldSupportCreatingMultipleLoops() {
        val factory: MobiusLoop.Factory<String, Int, Boolean> = loop(UPDATE, HANDLER)

        // one
        loop = factory.startFrom("first")
        loop.dispatchEvent(97)
        await().atMost(Duration.ofSeconds(1)).until({ loop.mostRecentModel }, `is`("first97"))
        loop.dispose()

        // then another one
        loop = factory.startFrom("second")
        loop.dispatchEvent(97)
        await().atMost(Duration.ofSeconds(1)).until({ loop.mostRecentModel }, `is`("second97"))
    }

    @Test
    fun shouldAllowStartingWithEffects() {
        val runner = TestableWorkRunner()
        loop = loop(UPDATE, HANDLER).effectRunner { runner }
            .startFrom(MY_MODEL, setOf(false))
        await().atMost(Duration.ofSeconds(1)).until { runner.runCounter.get() == 1 }
    }

    @Suppress("DEPRECATION")
    @Test
    fun shouldDisallowBothInitAndStartEffects() {
        val startEffects = setOf(true)
        val init =
            Init { model: String ->
                first(
                    model,
                    startEffects
                )
            }
        val factory: MobiusLoop.Factory<String, Int, Boolean> = loop(UPDATE, HANDLER).init(init)
        val error = assertFails { factory.startFrom(MY_MODEL, startEffects) }
        assertTrue(error.message?.contains("has init defined") ?: false)
    }

    private class TestableWorkRunner : WorkRunner {
        val runCounter = AtomicInteger()
        override fun post(runnable: Runnable) {
            try {
                executorService.submit(runnable).get()
                runCounter.incrementAndGet()
                LoggerFactory.getLogger(TestableWorkRunner::class.java).debug("runcounter: " + runCounter.get())
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }

        override fun dispose() {}

        companion object {
            private val executorService = Executors.newSingleThreadExecutor()
        }
    }

    private class ConnectableTestEventSource : Connectable<String, Int> {
        lateinit var consumer: Consumer<Int>

        @Nonnull
        override fun connect(output: Consumer<Int>): Connection<String> {
            consumer = output
            return object : Connection<String> {
                override fun accept(value: String) {}
                override fun dispose() {}
            }
        }
    }

    private class TestEventSource : EventSource<Int> {
        lateinit var consumer: Consumer<Int>

        override fun subscribe(eventConsumer: Consumer<Int>): Disposable {
            consumer = eventConsumer
            return Disposable {
                // do nothing
            }
        }
    }

    private class TestLogger : MobiusLoop.Logger<String, Int, Boolean> {
        val history: MutableList<String> = ArrayList()
        override fun beforeInit(model: String) {
            history.add(String.format("before init: %s", model))
        }

        override fun afterInit(model: String, result: First<String, Boolean>) {
            history.add(String.format("after init: %s, %s", model, result))
        }

        override fun exceptionDuringInit(model: String, exception: Throwable) {
            history.add(String.format("init error: %s, %s", model, exception))
        }

        override fun beforeUpdate(model: String, event: Int) {
            history.add(String.format("before update: %s, %s", model, event))
        }

        override fun afterUpdate(model: String, event: Int, result: Next<String, Boolean>) {
            history.add(String.format("after update: %s, %s, %s", model, event, result))
        }

        override fun exceptionDuringUpdate(model: String, event: Int, exception: Throwable) {
            history.add(String.format("update error: %s, %s, %s", model, event, exception))
        }
    }
}