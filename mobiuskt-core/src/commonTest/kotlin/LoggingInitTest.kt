package kt.mobius

import kt.mobius.First.Companion.first
import kotlin.test.*

class LoggingInitTest {
    private lateinit var loggingInit: LoggingInit<String, Int>
    private lateinit var logger: CapturingLogger<String, Boolean, Int>
    @BeforeTest
    fun setUp() {
        val delegate = Init<String, Int> { model -> first(model) }
        logger = CapturingLogger()
        loggingInit = LoggingInit(delegate, logger)
    }

    @Test
    fun shouldLogBeforeInit() {
        loggingInit.init("tha modell")
        assertContains(logger.beforeInits, "tha modell")
    }

    @Test
    fun shouldLogAfterInit() {
        loggingInit.init("tha modell")
        assertContains(logger.afterInits, CapturingLogger.AfterInitArgs("tha modell", first("tha modell")))
    }

    @Test
    fun shouldReportExceptions() {
        val expected = RuntimeException("expected")
        loggingInit = LoggingInit({ throw expected }, logger)
        try {
            loggingInit.init("log this plx")
        } catch (e: Exception) {
            // ignore
        }
        assertContains(logger.initErrors, CapturingLogger.InitErrorArgs("log this plx", expected))
    }

    @Test
    fun shouldPropagateExceptions() {
        val expected = RuntimeException("expected")
        loggingInit = LoggingInit({ throw expected }, logger)
        val error = assertFails { loggingInit.init("hi") }
        assertEquals(expected, error)
    }
}