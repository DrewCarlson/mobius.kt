package kt.mobius

import kotlin.test.*


class LoggingUpdateTest {
    private lateinit var loggingUpdate: LoggingUpdate<String, Int, Boolean>
    private lateinit var logger: CapturingLogger<String, Int, Boolean>

    @BeforeTest
    fun setUp() {
        logger = CapturingLogger()
        loggingUpdate = LoggingUpdate(
            { model, event ->
                Next.next("$model-", setOf(event % 2 == 0))
            },
            logger
        )
    }

    @Test
    fun shouldLogBeforeUpdate() {
        loggingUpdate.update("mah model", 1)
        assertContains(logger.beforeUpdates, CapturingLogger.BeforeUpdateArgs("mah model", 1))
    }

    @Test
    fun shouldLogAfterUpdate() {
        loggingUpdate.update("mah model", 1)
        assertContains(
            logger.afterUpdates,
            CapturingLogger.AfterUpdateArgs("mah model", 1, Next.next("mah model-", setOf(false)))
        )
    }

    @Test
    fun shouldReportExceptions() {
        val expected = RuntimeException("expected")
        loggingUpdate = LoggingUpdate({ _, _ -> throw expected }, logger)
        try {
            loggingUpdate.update("log this plx", 13)
        } catch (e: Exception) {
            // ignore
        }
        assertContains(logger.updateErrors, CapturingLogger.UpdateErrorArgs("log this plx", 13, expected))
    }

    @Test
    fun shouldPropagateExceptions() {
        val expected = RuntimeException("expected")
        loggingUpdate = LoggingUpdate({ _, _ -> throw expected }, logger)
        val error = assertFails { loggingUpdate.update("hi", 7) }
        assertEquals(expected, error)
    }
}