package kt.mobius.android

import android.os.Looper
import kt.mobius.android.runners.LooperWorkRunner
import kt.mobius.android.runners.MainThreadWorkRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
public class LooperWorkRunnerTest {

    @Test
    public fun testExecutesTask() {
        val runner = LooperWorkRunner.using(Looper.getMainLooper())
        var run = false

        runner.post { run = true }

        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(run)

        runner.dispose()
    }

    @Test
    public fun testDoesNotExecuteTaskWhenDisposed() {
        val runner = LooperWorkRunner.using(Looper.getMainLooper())
        var run = false

        runner.post { run = true }

        runner.dispose()

        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(run)
    }

    @Test
    public fun testRejectsRunnableAfterDispose() {
        val runner = LooperWorkRunner.using(Looper.getMainLooper())

        runner.dispose()
        runner.post { }

        assertTrue(shadowOf(Looper.getMainLooper()).isIdle)
    }

    @Test
    public fun testMainThreadWorkRunnerExecutesTask() {
        val runner = MainThreadWorkRunner.create()
        var run = false

        runner.post { run = true }

        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(run)

        runner.dispose()
    }

    @Test
    public fun testMainThreadWorkRunnerDoesNotExecuteTaskWhenDisposed() {
        val runner = MainThreadWorkRunner.create()
        var run = false

        runner.post { run = true }

        runner.dispose()

        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(run)
    }

    @Test
    public fun testMainThreadWorkRunnerRejectsRunnableAfterDispose() {
        val runner = MainThreadWorkRunner.create()

        runner.dispose()
        runner.post { }

        assertTrue(shadowOf(Looper.getMainLooper()).isIdle)
    }
}
