package kt.mobius.runners

import com.google.common.util.concurrent.Uninterruptibles.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExecutorServiceWorkRunnerTest {

    private lateinit var underTest: ExecutorServiceWorkRunner

    @Rule
    @JvmField
    val thrown = ExpectedException.none()!!

    @Before
    fun setUp() {
        underTest = ExecutorServiceWorkRunner(Executors.newSingleThreadExecutor())
    }

    @Test
    fun shouldNotReturnFromDisposeUntilFinishedRunning() {
        val blockBackground = Semaphore(0)
        val blockUnderTest = Semaphore(0)
        val blockMainThread = Semaphore(0)

        val output = CopyOnWriteArrayList<Int>()

        underTest.post(
            object : Runnable {
                override fun run() {
                    output.add(1)
                    blockBackground.release()
                    blockUnderTest.acquireUninterruptibly()
                    output.add(3)
                    blockMainThread.release()
                }
            })

        val backgroundWorkRunner =
            ExecutorServiceWorkRunner(Executors.newSingleThreadExecutor())
        backgroundWorkRunner.post(
            object : Runnable {
                override fun run() {
                    blockBackground.acquireUninterruptibly()
                    output.add(2)
                    blockUnderTest.release()
                }
            })

        blockMainThread.acquire()
        underTest.dispose()
        output.add(4)

        Thread.sleep(40) // wait a bit and make sure nothing else is added after the 4

        assertEquals(listOf(1, 2, 3, 4), output)
    }

    @Test
    fun disposingShouldStopUnderlyingExecutorService() {
        val service = Executors.newSingleThreadExecutor()

        underTest = ExecutorServiceWorkRunner(service)
        underTest.dispose()

        assertTrue(service.isTerminated)
    }

    @Test
    fun tasksShouldBeRejectedAfterDispose() {
        val service = Executors.newSingleThreadExecutor()

        underTest = ExecutorServiceWorkRunner(service)
        underTest.dispose()

        thrown.expect(RejectedExecutionException::class.java)

        underTest.post(
            object : Runnable {
                override fun run() {
                    println("ERROR: this shouldn't run/be printed!")
                }
            })
    }

    @Test
    fun disposeShouldContinueDespiteUnterminatedTask() {
        val alwaysTrue = AtomicBoolean(true)

        underTest.post(
            object : Runnable {
                override fun run() {
                    while (alwaysTrue.get()) {
                        sleepUninterruptibly(100, TimeUnit.MILLISECONDS)
                    }
                }
            })

        // should terminate with no exceptions, but should log a warning about an unterminated task
        underTest.dispose()
    }

    @Test
    fun disposeShouldContinueDespiteUnterminatedAndQueuedTasks() {
        val alwaysTrue = AtomicBoolean(true)

        underTest.post(
            object : Runnable {
                override fun run() {
                    while (alwaysTrue.get()) {
                        sleepUninterruptibly(100, TimeUnit.MILLISECONDS)
                    }
                }
            })
        underTest.post(
            object : Runnable {
                override fun run() {
                    System.err.println("Don't want to see this!")
                }
            })

        // should terminate with no exceptions, but should log a warning about a queued and an
        // unterminated task
        underTest.dispose()
    }
}
