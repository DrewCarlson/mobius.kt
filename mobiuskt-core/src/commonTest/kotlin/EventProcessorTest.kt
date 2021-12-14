package kt.mobius

import kt.mobius.MobiusStore.Companion.create
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventProcessorTest {
    private lateinit var underTest: EventProcessor<String, Int, Long>
    private lateinit var effectConsumer: RecordingConsumer<Long>
    private lateinit var stateConsumer: RecordingConsumer<String>

    @BeforeTest
    fun setUp() {
        effectConsumer = RecordingConsumer()
        stateConsumer = RecordingConsumer()
        underTest = EventProcessor(create(createUpdate(), "init!"), effectConsumer, stateConsumer)
    }

    @Test
    fun shouldEmitStateIfStateChanged() {
        underTest.update(1)
        stateConsumer.assertValues("init!->1")
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
        stateConsumer.assertValues("init!->1", "init!->1->2")
    }

    @Test
    fun shouldEmitEffectsWhenStateChanges() {
        effectConsumer.clearValues()
        underTest.update(3)
        effectConsumer.assertValuesInAnyOrder(10L, 20L, 30L)
    }

    private fun createUpdate(): Update<String, Int, Long> {
        return Update { model: String, event: Int ->
            if (event == 0) {
                Next.noChange()
            } else {
                val effects = hashSetOf<Long>()
                for (i in 0 until event) {
                    effects.add(10L * (i + 1))
                }
                Next.next("$model->$event", effects)
            }
        }
    }
}
