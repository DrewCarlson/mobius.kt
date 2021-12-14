package kt.mobius

import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.functions.Consumer
import kt.mobius.runners.WorkRunners
import kt.mobius.testdomain.EventWithSafeEffect
import kt.mobius.testdomain.TestEffect
import kt.mobius.testdomain.TestEvent
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.test.assertTrue


class MobiusLoopBehaviorWithEventSources : MobiusLoopTest() {

    @Test
    fun invokesEventSourceOnlyOnModelUpdates() {
        val eventSource = ModelRecordingConnectableEventSource()

        update = Update { s, e ->
            if (e is EventWithSafeEffect) {
                next("$s->$e")
            } else {
                noChange()
            }
        }

        val loop =
            Mobius.loop(update, FakeEffectHandler())
                .eventRunner(WorkRunners::immediate)
                .effectRunner(WorkRunners::immediate)
                .eventSource(eventSource)
                .startFrom("init")

        loop.dispatchEvent(TestEvent("This"))
        loop.dispatchEvent(EventWithSafeEffect("1"))
        loop.dispatchEvent(TestEvent("will not"))
        loop.dispatchEvent(EventWithSafeEffect("2"))
        loop.dispatchEvent(TestEvent("change"))
        loop.dispatchEvent(TestEvent("state"))

        eventSource.receivedModels.assertValues("init", "init->1", "init->1->2")
        assertThat(eventSource.receivedModels.valueCount(), `is`(3))
    }

    @Test
    fun processesEventsFromEventSources() {
        val eventSource = ModelRecordingConnectableEventSource()

        mobiusLoop =
            MobiusLoop.create(
                update,
                startModel,
                startEffects,
                effectHandler,
                eventSource,
                immediateRunner,
                immediateRunner
            )
        observer = RecordingModelObserver()
        mobiusLoop.observe(observer)
        eventSource.consumer.accept(TestEvent(1.toString()))
        eventSource.consumer.accept(TestEvent(2.toString()))
        eventSource.consumer.accept(TestEvent(3.toString()))
        observer.assertStates("init", "init->1", "init->1->2", "init->1->2->3")
    }

    @Test
    fun disposesOfEventSourceWhenDisposed() {
        val eventSource = ModelRecordingConnectableEventSource()
        mobiusLoop = MobiusLoop.create(
            update,
            startModel,
            startEffects,
            effectHandler,
            eventSource,
            immediateRunner,
            immediateRunner
        )

        mobiusLoop.dispose()
        assertTrue(eventSource.disposed)
    }

    @Test
    fun shouldSupportEventSourcesThatEmitOnConnect() {
        // given an event source that immediately emits an event (id 1) on connect
        val eventSource = ImmediateEmitter()

        // when we create a mobius loop
        mobiusLoop =
            MobiusLoop.create(
                update,
                startModel,
                startEffects,
                effectHandler,
                eventSource,
                immediateRunner,
                immediateRunner
            )

        // then the event source should receive the initial model as well as the one following from
        // its emitted event
        eventSource.receivedModels.assertValues("init", "init->1")
    }

    class ModelRecordingConnectableEventSource : Connectable<String, TestEvent> {

        val receivedModels = RecordingConsumer<String>()
        var disposed: Boolean = false
        lateinit var consumer: Consumer<TestEvent>

        override fun connect(output: Consumer<TestEvent>): Connection<String> {
            consumer = output
            return object : Connection<String> {
                override fun accept(value: String) {
                    receivedModels.accept(value)
                }

                override fun dispose() {
                    disposed = true
                }
            }
        }
    }

    class ImmediateEmitter : Connectable<String, TestEvent> {
        val receivedModels = RecordingConsumer<String>()

        override fun connect(output: Consumer<TestEvent>): Connection<String> {
            output.accept(EventWithSafeEffect("1"))
            return object : Connection<String> {
                override fun accept(value: String) {
                    receivedModels.accept(value)
                }

                override fun dispose() {
                }
            }
        }
    }
}