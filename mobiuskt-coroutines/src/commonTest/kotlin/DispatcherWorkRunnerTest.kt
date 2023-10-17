package kt.mobius.flow

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunners
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DispatcherWorkRunnerTest {

    @Test
    fun workIsExecutedBeforeDispose() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val workRunner = WorkRunners.fromDispatcher(testDispatcher)

        val runnable = TestRunnable()
        workRunner.post(runnable)

        assertFalse(runnable.hasRun)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(runnable.hasRun)
    }

    @Test
    fun workIsIgnoredAfterDispose() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val workRunner = WorkRunners.fromDispatcher(testDispatcher)

        val runnable = TestRunnable()
        workRunner.post(runnable)

        assertFalse(runnable.hasRun)
        workRunner.dispose()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(runnable.hasRun)
    }

    private class TestRunnable : Runnable {
        var hasRun: Boolean = false
            private set

        override fun run() {
            hasRun = true
        }
    }
}
