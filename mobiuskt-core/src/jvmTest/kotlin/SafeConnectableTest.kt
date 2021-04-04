package kt.mobius

import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeConnectableTest {

    lateinit var recordingConsumer: RecordingConsumer<String>
    lateinit var safeConsumer: Connection<Int>
    lateinit var blockEffectPerformer: Semaphore
    lateinit var signalEffectHasBeenPerformed: Semaphore
    private lateinit var blockableConnection: BlockableConnection

    private lateinit var underTest: SafeConnectable<Int, String>

    val executorService = Executors.newSingleThreadExecutor()

    @Before
    fun setUp() {
        blockEffectPerformer = Semaphore(0)
        signalEffectHasBeenPerformed = Semaphore(0)

        recordingConsumer = RecordingConsumer()
        blockableConnection = BlockableConnection(recordingConsumer)

        underTest =
            SafeConnectable(
                Connectable {
                    blockableConnection
                })
    }

    @Test
    fun delegatesEffectsToActualSink() {
        safeConsumer = underTest.connect(recordingConsumer)
        safeConsumer.accept(1)
        recordingConsumer.assertValues("Value is: 1")
    }

    @Test
    fun delegatesDisposeToActualSink() {
        safeConsumer = underTest.connect(recordingConsumer)
        safeConsumer.dispose()
        assertTrue(blockableConnection.disposed)
    }

    @Test
    fun discardsEventsAfterDisposal() {
        safeConsumer = underTest.connect(recordingConsumer)

        // given the effect performer is blocked
        blockableConnection.block = true

        // when an effect is requested
        val effectPerformedFuture =
            executorService.submit {
                safeConsumer.accept(1)
            }

        // and the sink is disposed
        safeConsumer.dispose()

        // before the effect gets performed
        // (needs permitting the blocked effect performer to proceed)
        blockEffectPerformer.release()

        // (get the result of the future to ensure the effect has been performed, also propagating
        // exceptions if any - result should happen quickly, but it's good to have a timeout in case
        // something is messed up)
        effectPerformedFuture.get(10, TimeUnit.SECONDS)

        // then no events are emitted
        recordingConsumer.assertValues()
    }

    @Test
    fun discardsEffectsAfterDisposal() {
        // given a disposed sink
        safeConsumer = underTest.connect(recordingConsumer)
        safeConsumer.dispose()

        // when an effect is performed
        safeConsumer.accept(1)

        // then no effects or events happen
        blockableConnection.assertEffects()
        recordingConsumer.assertValues()
    }

    inner class BlockableConnection(
        private val eventConsumer: RecordingConsumer<String>
    ) : Connection<Int> {

        private val recordedEffects = arrayListOf<Int>()

        var disposed = false
            private set

        @Volatile
        var block = false

        fun assertEffects(vararg values: Int) {
            assertEquals(values.asList(), recordedEffects)
        }

        override fun accept(effect: Int) {
            if (block) {
                try {
                    if (!blockEffectPerformer.tryAcquire(5, TimeUnit.SECONDS)) {
                        throw IllegalStateException("timed out waiting for effect performer unblock")
                    }
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
            recordedEffects.add(effect);
            eventConsumer.accept("Value is: $effect")
            signalEffectHasBeenPerformed.release()
        }

        override fun dispose() {
            disposed = true
            signalEffectHasBeenPerformed.release()
        }
    }
}
