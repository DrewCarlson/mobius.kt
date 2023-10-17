package kt.mobius.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.test.runTest
import kt.mobius.*
import kt.mobius.First.Companion.first
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class ComposeMobiusLoopTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun test_StartModel_IsSet() = runTest {
        compose.setContent { testUi(100) }

        compose
            .onNodeWithTag("model")
            .assertTextEquals("100")
    }

    @Test
    fun test_Event_UpdatesModel() = runTest {
        compose.setContent { testUi() }

        compose
            .onNodeWithText("inc")
            .performClick()

        compose
            .onNodeWithTag("model")
            .assertTextEquals("2")

        compose
            .onNodeWithText("dec")
            .performClick()

        compose
            .onNodeWithTag("model")
            .assertTextEquals("1")
    }

    @Test
    fun test_Init_IsApplied() = runTest {
        compose.setContent {
            testUi(init = { first(100) })
        }
        compose
            .onNodeWithTag("model")
            .assertTextEquals("100")
    }

    @Test
    fun test_StartModelChange_DoesNotResetLoop() = runTest {
        compose.setContent {
            val startModel = mutableStateOf(1)
            testUi(startModel = startModel.value)

            LaunchedEffect(Unit) {
                startModel.value = 100
            }
        }

        compose.awaitIdle()

        compose
            .onNodeWithTag("model")
            .assertTextEquals("1")
    }

    @Test
    fun test_NewLoop_WithUpdatedStartModel() = runTest {
        val isDisplayed = mutableStateOf(true)
        val startModel = mutableStateOf(0)
        compose.setContent {
            if (isDisplayed.value) {
                testUi(startModel = startModel.value)
            }
        }
        compose
            .onNodeWithTag("model")
            .assertTextEquals("0")

        isDisplayed.value = false
        startModel.value = 1
        compose.awaitIdle()

        isDisplayed.value = true
        compose.awaitIdle()

        compose
            .onNodeWithTag("model")
            .assertTextEquals("1")
    }

    @Test
    fun test_WhenComposableIsDisposed_LoopIsDisposed() = runTest {
        val isDisplayed = mutableStateOf(true)
        val isDisposed = mutableStateOf(false)
        compose.setContent {
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
        compose.awaitIdle()
        assertTrue(isDisposed.value)
    }

    @Test
    fun test_ExistingLoop_WithUpdatedStartModel() = runTest {
        val startModel = mutableStateOf(0)
        compose.setContent {
            testUi(startModel = startModel.value)
        }
        compose
            .onNodeWithTag("model")
            .assertTextEquals("0")

        startModel.value = 1
        compose.awaitIdle()

        compose
            .onNodeWithTag("model")
            .assertTextEquals("0")
    }

    @Composable
    private fun testUi(
        startModel: Int = 1,
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
        Column {
            Text(
                text = model.value.toString(),
                modifier = Modifier.testTag("model")
            )
            Button(onClick = { eventConsumer(1) }) { Text("inc") }
            Button(onClick = { eventConsumer(-1) }) { Text("dec") }
        }
    }
}
