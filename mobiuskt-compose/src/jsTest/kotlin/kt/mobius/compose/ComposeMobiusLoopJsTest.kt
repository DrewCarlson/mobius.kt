package kt.mobius.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import kt.mobius.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.w3c.dom.HTMLSpanElement
import kotlin.test.*

@OptIn(ComposeWebExperimentalTestsApi::class)
class ComposeMobiusLoopJsTest {

    @Test
    fun test_StartModel_IsSet() = runTest {
        composition { testUi(100) }

        val modelSpan = assertNotNull(root.querySelector("#model"))

        assertEquals("100", modelSpan.textContent)
    }

    @Test
    fun test_Event_UpdatesModel() = runTest {
        composition { testUi(1) }

        val incSpan: HTMLSpanElement = assertIs(assertNotNull(root.querySelector("#inc")))
        val decSpan: HTMLSpanElement = assertIs(assertNotNull(root.querySelector("#dec")))
        val modelSpan = assertNotNull(root.querySelector("#model"))

        incSpan.click()
        waitForRecompositionComplete()
        assertEquals("2", modelSpan.textContent)
        decSpan.click()
        waitForRecompositionComplete()
        assertEquals("1", modelSpan.textContent)
    }

    @Test
    fun test_Init_IsApplied() = runTest {
        composition { testUi(0, init = { First.first(100) }) }
        val modelSpan = assertNotNull(root.querySelector("#model"))
        assertEquals("100", modelSpan.textContent)
    }

    @Test
    fun test_StartModelChange_DoesNotResetLoop() = runTest {
        val startModel = mutableStateOf(1)
        composition {
            testUi(startModel = startModel.value)
            LaunchedEffect(Unit) {
                startModel.value = 100
            }
        }
        val modelSpan = assertNotNull(root.querySelector("#model"))

        waitForRecompositionComplete()

        assertEquals("1", modelSpan.textContent)
    }

    @Test
    fun test_NewLoop_WithUpdatedStartModel() = runTest {
        val isDisplayed = mutableStateOf(true)
        val startModel = mutableStateOf(0)
        composition {
            if (isDisplayed.value) {
                testUi(startModel = startModel.value)
            }
        }
        val modelSpan = assertNotNull(root.querySelector("#model"))

        assertEquals("0", modelSpan.textContent)

        isDisplayed.value = false
        startModel.value = 1
        waitForRecompositionComplete()

        isDisplayed.value = true
        waitForRecompositionComplete()

        val modelSpan2 = assertNotNull(root.querySelector("#model"))
        assertEquals("1", modelSpan2.textContent)
    }

    @Test
    fun test_WhenComposableIsDisposed_LoopIsDisposed() = runTest {
        val isDisplayed = mutableStateOf(true)
        val isDisposed = mutableStateOf(false)
        composition {
            if (isDisplayed.value) {
                rememberMobiusLoop(0) {
                    Mobius.loop(
                        update = Update<Int, Int, Unit> { model, event -> Next.next(model + event) },
                        effectHandler = {
                            object : Connection<Unit> {
                                override fun accept(value: Unit) = Unit
                                override fun dispose() {
                                    isDisposed.value = true
                                }
                            }
                        }
                    ).logger(SimpleLogger("Test"))
                }
            }
        }

        isDisplayed.value = false
        waitForRecompositionComplete()
        assertTrue(isDisposed.value)
    }

    @Test
    fun test_ExistingLoop_WithUpdatedStartModel() = runTest {
        val startModel = mutableStateOf(0)
        composition {
            testUi(startModel = startModel.value)
        }
        val modelSpan = assertNotNull(root.querySelector("#model"))

        assertEquals("0", modelSpan.textContent)

        startModel.value = 1
        waitForRecompositionComplete()

        assertEquals("0", modelSpan.textContent)
    }

    @Composable
    private fun testUi(
        startModel: Int,
        init: Init<Int, Unit>? = null
    ) {
        val (model, eventConsumer) = rememberMobiusLoop(startModel, init) {
            Mobius.loop<Int, Int, Unit>(
                update = { model, event -> Next.next(model + event) },
                effectHandler = {
                    object : Connection<Unit> {
                        override fun accept(value: Unit) = Unit
                        override fun dispose() = Unit
                    }
                }
            ).logger(SimpleLogger("Test"))
        }
        Div {
            Span({ id("model") }) {
                Text(model.value.toString())
            }
            Span({
                id("inc")
                onClick { eventConsumer(1) }
            }) {
                Text("inc")
            }
            Span({
                id("dec")
                onClick { eventConsumer(-1) }
            }) {
                Text("dec")
            }
        }
    }
}
