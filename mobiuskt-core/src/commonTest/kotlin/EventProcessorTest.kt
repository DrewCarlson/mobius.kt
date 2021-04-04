package kt.mobius

import kt.mobius.MobiusStore.Companion.create
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class EventProcessorTest {
    private lateinit var underTest: EventProcessor<String, Int, Long>
    private lateinit var effectConsumer: RecordingConsumer<Long>
    private lateinit var stateConsumer: RecordingConsumer<String>

    @BeforeTest
    fun setUp() {
        effectConsumer = RecordingConsumer()
        stateConsumer = RecordingConsumer()
        underTest = EventProcessor(createStore(), effectConsumer, stateConsumer)
        underTest.init()
    }

    @Test
    fun shouldEmitStateIfStateChanged() {
        underTest.update(1)
        stateConsumer.assertValues("init!", "init!->1")
    }

    @Test
    fun shouldNotEmitStateIfStateNotChanged() {
        stateConsumer.clearValues()
        underTest.update(0)
        stateConsumer.assertValues()
    }

    @Test
    fun shouldOnlyEmitStateStateChanged() {
        underTest.update(0)
        underTest.update(1)
        underTest.update(0)
        underTest.update(2)
        stateConsumer.assertValues("init!", "init!->1", "init!->1->2")
    }

    @Test
    fun shouldEmitEffectsWhenStateChanges() {
        effectConsumer.clearValues()
        underTest.update(3)
        effectConsumer.assertValuesInAnyOrder(10L, 20L, 30L)
    }

    @Test
    fun shouldEmitStateDuringInit() {
        stateConsumer.assertValues("init!")
    }

    @Test
    fun shouldEmitEffectsDuringInit() {
        effectConsumer.assertValuesInAnyOrder(15L, 25L, 35L)
    }

    @Test
    fun shouldQueueUpdatesReceivedBeforeInit() {
        stateConsumer.clearValues()
        underTest = EventProcessor(createStore(), effectConsumer, stateConsumer)

        underTest.update(1)
        underTest.update(2)
        underTest.update(3)

        underTest.init()

        stateConsumer.assertValues("init!", "init!->1", "init!->1->2", "init!->1->2->3")
    }

    @Test
    fun shouldDisallowDuplicateInitialisation() {
        assertFailsWith(IllegalStateException::class) {
            underTest.init()
        }
    }

    fun createStore(): MobiusStore<String, Int, Long> {
        return create(Init { model ->
            First.first("$model!", setOf(15L, 25L, 35L))
        }, Update { model: String, event: Int ->
            if (event == 0) {
                Next.noChange()
            } else {
                val effects = hashSetOf<Long>()
                for (i in 0 until event) {
                    effects.add(10L * (i + 1))
                }
                Next.next("$model->$event", effects)
            }
        }, "init")
    }
}
