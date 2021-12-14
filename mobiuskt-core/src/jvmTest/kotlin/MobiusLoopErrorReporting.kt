package kt.mobius

import kt.mobius.MobiusLoop.Companion.create
import kt.mobius.runners.ExecutorServiceWorkRunner
import kt.mobius.runners.WorkRunner
import kt.mobius.testdomain.TestEvent
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue


class MobiusLoopErrorReporting : MobiusLoopTest() {

    @Test
    fun shouldIncludeEventInExceptionWhenDispatchFails() {
        // given a loop
        observer = RecordingModelObserver()
        val executorService = Executors.newSingleThreadExecutor()
        val eventRunner: WorkRunner = ExecutorServiceWorkRunner(executorService)
        mobiusLoop = create(
            update,
            startModel,
            startEffects,
            effectHandler,
            eventSource,
            eventRunner,
            immediateRunner
        )

        // whose event workrunner has been disposed.
        eventRunner.dispose()

        // when an event is dispatched,
        // then the exception contains a description of the event.
        val error = assertFails {
            mobiusLoop.dispatchEvent(TestEvent("print me in the exception message"))
        }

        assertTrue(error.message?.contains("print me in the exception message") ?: false)
    }
}